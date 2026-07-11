package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import dev.architectury.event.EventResult;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.BulletBinder;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.compat.FirstAidCompatHandler;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.Optional;

public class PlateDamageEvent {
    private static boolean modify(
            LivingEntity livingEntity,
            DamageSource damageSource,
            float amount,
            ItemStack[] stacksInSlot
    ) {
        damage(livingEntity, damageSource, amount, stacksInSlot);
        return false;
    }

    private static void damage(LivingEntity livingEntity, DamageSource damageSource, float amount, ItemStack[] stacksInSlot) {
        if (damageSource.is(ModDamageTypes.BULLET_IGNORE_ARMOR) && damageSource.typeHolder().containsTag(DamageTypeTags.BYPASSES_ARMOR))
            return;
        float durabilityLossFactor = 0.1f;
        float defaultArmorDamage = Math.min(amount * 0.1f, 10);
        int durabilityLossAmount = 1;
        ItemStack stackInSlot = stacksInSlot[0];
        ItemStack stackInSlot2 = ItemStack.EMPTY;
        if (stacksInSlot.length > 1) {
            stackInSlot2 = stacksInSlot[1];
        }
        actuallyDamage(livingEntity, damageSource, stackInSlot, defaultArmorDamage, durabilityLossFactor, durabilityLossAmount);
        if (!stackInSlot2.isEmpty()) {
            actuallyDamage(livingEntity, damageSource, stackInSlot2, defaultArmorDamage, durabilityLossFactor, durabilityLossAmount);
        }
    }

    private static void actuallyDamage(LivingEntity livingEntity, DamageSource damageSource, ItemStack stackInSlot, float defaultArmorDamage, float durabilityLossFactor, int durabilityLossAmount) {
        if (!stackInSlot.isEmpty() && (damageSource.is(ModDamageTypes.BULLETS_TAG) || damageSource.is(ZDamageTypes.ZC_DAMAGE))) {
            if (stackInSlot.getItem() instanceof ICombatArmorItem armorProvider) {
                if (!(damageSource.getDirectEntity() instanceof EntityKineticBullet bullet)) return;
                AmmoInjector.AmmoContext ammoContext = BulletBinder.getContext(bullet);
                float caliberArmorDamage;
                int hits = stackInSlot.getOrCreateTag().getInt("hits");
                if (ammoContext != null) {
                    CaliberVariantDamageHelper.Caliber caliber = ammoContext.caliber();
                    if (caliber.armorDamage() != 0) {
                        caliberArmorDamage = getArmorDamage(caliber, armorProvider, caliber.armorDamage());
                    } else {
                        caliberArmorDamage = getArmorDamage(caliber, armorProvider, defaultArmorDamage);
                    }
                    int armorLoss = armorProvider.generateLoss(caliberArmorDamage, durabilityLossFactor, hits);
                    if (armorLoss <= 0) {
                        durabilityLossAmount = ICombatArmorItem.generateLossDefault(caliberArmorDamage, durabilityLossFactor, hits);
                    } else {
                        durabilityLossAmount = armorLoss;
                    }
                }

                hits++;
                stackInSlot.getOrCreateTag().putInt("hits", hits);
                EntityKineticBullet.EntityResult result = EventUtil.getHitResult(damageSource);

                if (result != null && result.isHeadshot()) {
                    return;
                }

                FirstAidCompatHandler firstAidCompatHandler = FirstAidCompatHandler.create(livingEntity, damageSource);
                if (firstAidCompatHandler != null && firstAidCompatHandler.getLimbsApplicable()) return;
                stackInSlot.hurtAndBreak(durabilityLossAmount, livingEntity, lv -> lv.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f));
            }
        }
    }

    private static float getArmorDamage(CaliberVariantDamageHelper.Caliber caliber, ICombatArmorItem provider, float baseDamage) {
        int absorb = provider.getAbsorb() == 0 ? 1 : provider.getAbsorb();
        return caliber.penetrationClass() * baseDamage * ((float) caliber.penetrationClass() / absorb);
    }

    public static void damageHelmet(EntityHurtByGunEvent event) {
        boolean isHeadshot = event.isHeadShot();
        if (!isHeadshot) return;
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        float amount = event.getAmount();
        entity.ifPresent(e -> {
            if (e instanceof LivingEntity livingEntity) {
                ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                float durabilityLossFactor = 1;
                int hits = stack.getOrCreateTag().getInt("hits") + 1;
                int durabilityLossAmount;
                if (stack.getItem() instanceof ICombatArmorItem durabilityLossProvider && stack.getItem() instanceof HelmetInfoProvider) {
                    durabilityLossAmount = durabilityLossProvider.generateLoss(amount, durabilityLossFactor, hits);
                    stack.getOrCreateTag().putInt("hits", hits);
                    stack.hurtAndBreak(durabilityLossAmount, livingEntity, broken -> livingEntity.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f));
                }
            }
        });
    }

    public static EventResult register(LivingEntity entity, DamageSource damageSource, float amount) {
        return modify(entity, damageSource, amount, EventUtil.getHitBodyPartStack(entity, damageSource)) ? EventResult.interruptFalse() : EventResult.pass();
    }
}
