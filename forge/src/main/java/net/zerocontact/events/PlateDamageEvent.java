package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class PlateDamageEvent {
    private static boolean isHeadshot;

    private static void DamagePlateModifier(
            ICuriosItemHandler iCuriosItemHandler,
            LivingEntity livingEntity,
            DamageSource damageSource,
            float amount,
            String identifier
    ) {
        if (isHeadshot) return;
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stackInSlot = stacksHandler.getStacks().getStackInSlot(0);
            damage(livingEntity, damageSource, amount, stackInSlot);
        });
    }

    private static void damage(LivingEntity livingEntity, DamageSource damageSource, float amount, ItemStack stackInSlot) {
        float durabilityLossFactor = 1;
        int hits = stackInSlot.getOrCreateTag().getInt("hits") + 1;
        int durabilityLossAmount = 1;
        if (!stackInSlot.isEmpty() && (damageSource.is(ModDamageTypes.BULLET) || damageSource.is(ModDamageTypes.BULLET_IGNORE_ARMOR))) {
            durabilityLossFactor = getDurabilityLossFactor(amount, durabilityLossFactor);
            if (stackInSlot.getItem() instanceof DurabilityLossProvider provider) {
                durabilityLossAmount = provider.generateLoss(amount, durabilityLossFactor, hits);
            }
            stackInSlot.getOrCreateTag().putInt("hits", hits);
            stackInSlot.hurtAndBreak(durabilityLossAmount, livingEntity, lv -> {
                lv.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f);
                ZeroContactLogger.LOG.info(lv.getName() + "的插板碎掉了！");
            });
        }
    }

    private static float getDurabilityLossFactor(float amount, float durabilityLossFactor) {
        switch (ProtectionLevelHelper.get((int) Math.floor(amount))) {
            case NIJIIA -> durabilityLossFactor = 0.1F;
            case NIJII -> durabilityLossFactor = 0.4F;
            case NIJIIIA -> durabilityLossFactor = 0.7F;
            case NIJIII -> durabilityLossFactor = 1.1F;
            case NIJIV -> durabilityLossFactor = 1.5F;
        }
        return isHeadshot ? durabilityLossFactor * 1.5f : durabilityLossFactor;
    }

    public static void DamageHelmet(EntityHurtByGunEvent event) {
        isHeadshot = event.isHeadShot();
        if (!isHeadshot) return;
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        float amount = event.getAmount();
        entity.ifPresent(e -> {
            if (e instanceof LivingEntity livingEntity) {
                ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                float durabilityLossFactor = 1;
                int hits = stack.getOrCreateTag().getInt("hits") + 1;
                int durabilityLossAmount = 1;
                if (stack.getItem() instanceof DurabilityLossProvider durabilityLossProvider && stack.getItem() instanceof HelmetInfoProvider) {
                    durabilityLossFactor = getDurabilityLossFactor(durabilityLossAmount, durabilityLossFactor);
                    durabilityLossAmount = durabilityLossProvider.generateLoss(amount, durabilityLossFactor, hits);
                    stack.getOrCreateTag().putInt("hits", hits);
                    stack.hurtAndBreak(durabilityLossAmount, livingEntity, broken -> {
                        livingEntity.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f);
                    });
                }
            }
        });
    }

    public static void DamagePlateRegister(LivingEntity entity, DamageSource damageSource, float amount) {
        if (EventUtil.isDamageSourceValid(damageSource)) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv -> DamagePlateModifier(inv, entity, damageSource, amount, EventUtil.idHitFromBack(entity, damageSource)));
        }
    }
}
