package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class PlateDamageEvent {
    private static void DamagePlateModifier(
            LivingEntity livingEntity,
            DamageSource damageSource,
            float amount,
            ItemStack plateSlot
    ) {
        if (plateSlot.isEmpty()) return;
        damage(livingEntity, damageSource, amount, plateSlot);
    }

    private static void damage(LivingEntity livingEntity, DamageSource damageSource, float amount, ItemStack stackInSlot) {
        float durabilityLossFactor = 1;
        int hits = stackInSlot.getOrCreateTag().getInt("hits") + 1;
        int durabilityLossAmount = 1;
        if (!stackInSlot.isEmpty() && (damageSource.is(ModDamageTypes.BULLETS_TAG))) {
            if (stackInSlot.getItem() instanceof ICombatArmorItem provider) {
                durabilityLossAmount = provider.generateLoss(amount, durabilityLossFactor, hits);
            }
            stackInSlot.getOrCreateTag().putInt("hits", hits);
            EntityKineticBullet.EntityResult result = EventUtil.getHitResult(damageSource);
            if (result != null && result.isHeadshot()) {
                return;
            }
            stackInSlot.hurtAndBreak(durabilityLossAmount, livingEntity, lv -> {
                lv.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f);
                ZeroContactLogger.LOG.debug("{}的插板碎掉了！", lv.getName());
            });
        }
    }

    public static void DamageHelmet(EntityHurtByGunEvent event) {
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

    public static void DamagePlateRegister(LivingEntity entity, DamageSource damageSource, float amount) {
        if (EventUtil.isDamageSourceValid(damageSource)) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv -> DamagePlateModifier(entity, damageSource, amount, EventUtil.idHitFromBack(entity, damageSource)));
        }
    }
}
