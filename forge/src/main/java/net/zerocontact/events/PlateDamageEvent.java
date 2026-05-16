package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.Optional;

import static net.zerocontact.item.armor.forge.BaseArmorGeoImpl.*;

public class PlateDamageEvent {
    private static void modifyDamage(
            LivingEntity livingEntity,
            DamageSource damageSource,
            float amount,
            ItemStack plateSlot,
            int plateIndex
    ) {
        if (plateSlot.isEmpty()) return;
        damage(livingEntity, damageSource, amount, plateSlot, plateIndex);
    }

    private static void damage(LivingEntity livingEntity, DamageSource damageSource, float amount, ItemStack stackInSlot, int plateIndex) {
        if (!stackInSlot.isEmpty() && (damageSource.is(ModDamageTypes.BULLETS_TAG) || damageSource.is(ZDamageTypes.ZC_DAMAGE))) {
            float durabilityLossFactor = 1;
            EntityKineticBullet.EntityResult result = EventUtil.getHitResult(damageSource);
            if (result != null && result.isHeadshot()) {
                return;
            }
            if (stackInSlot.getItem() instanceof PlateInfoProvider) {
                ItemStack armorStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                if (armorStack.getItem() instanceof ICombatArmorItem) {
                    BaseArmorGeoImpl.modifyPlateStack(
                            plateIndex,
                            livingEntity.getItemBySlot(EquipmentSlot.CHEST),
                            plateStack -> {
                                plateStack.hurtAndBreak(setLossAmount(plateStack, amount, durabilityLossFactor), livingEntity, lv -> lv.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f));
                                return plateStack;
                            });
                }
            } else {
                stackInSlot.hurtAndBreak(setLossAmount(stackInSlot, amount, durabilityLossFactor), livingEntity, lv -> {
                    lv.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f);
                });
            }

        }
    }

    private static int setLossAmount(ItemStack stackInSlot, float amount, float durabilityLossFactor) {
        int hits = stackInSlot.getOrCreateTag().getInt("hits") + 1;
        int durabilityLossAmount = 1;
        if (stackInSlot.getItem() instanceof ICombatArmorItem provider) {
            durabilityLossAmount = provider.generateLoss(amount, durabilityLossFactor, hits);
        }
        stackInSlot.getOrCreateTag().putInt("hits", hits);
        return durabilityLossAmount;
    }

    public static void modifyHelmet(EntityHurtByGunEvent event) {
        boolean isHeadshot = event.isHeadShot();
        if (!isHeadshot) return;
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        float amount = event.getAmount();
        entity.ifPresent(e -> {
            if (e instanceof LivingEntity livingEntity) {
                ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                float durabilityLossFactor = 1;
                int durabilityLossAmount;
                if (stack.getItem() instanceof HelmetInfoProvider) {
                    durabilityLossAmount = setLossAmount(stack, amount, durabilityLossFactor);
                    stack.hurtAndBreak(durabilityLossAmount, livingEntity, broken -> livingEntity.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f));
                }
            }
        });
    }

    public static void register(LivingEntity entity, DamageSource damageSource, float amount) {
        if (EventUtil.isDamageSourceValid(damageSource)) {
            if (!EventUtil.isHitFromBack(entity, damageSource)) {
                modifyDamage(entity, damageSource, amount, BaseArmorGeoImpl.getPlateStack(FRONT_PLATE_SLOT, entity.getItemBySlot(EquipmentSlot.CHEST)), FRONT_PLATE_SLOT);
            } else {
                modifyDamage(entity, damageSource, amount, BaseArmorGeoImpl.getPlateStack(BACK_PLATE_SLOT, entity.getItemBySlot(EquipmentSlot.CHEST)), BACK_PLATE_SLOT);
            }
        }
    }
}
