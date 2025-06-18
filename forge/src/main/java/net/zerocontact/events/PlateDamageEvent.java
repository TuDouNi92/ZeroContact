package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class PlateDamageEvent {

    private static void DamagePlateModifier(
            ICuriosItemHandler iCuriosItemHandler,
            LivingEntity livingEntity,
            DamageSource damageSource,
            float amount,
            String identifier
    ) {
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            float durabilityLossFactor = 1;
            int hits = stack.getOrCreateTag().getInt("hits") + 1;
            int durabilityLossAmount = 1;
            if (!stack.isEmpty() && (damageSource.is(ModDamageTypes.BULLET) || damageSource.is(ModDamageTypes.BULLET_IGNORE_ARMOR))) {
                if (PlateEntityHurtEvent.isHeadShot) return;
                durabilityLossFactor = getDurabilityLossFactor(amount, durabilityLossFactor);
                if (stack.getItem() instanceof DurabilityLossProvider provider) {
                    durabilityLossAmount = provider.generateLoss(amount, durabilityLossFactor, hits);
                }
            }
            stack.getOrCreateTag().putInt("hits", hits);
            stack.hurtAndBreak(durabilityLossAmount, livingEntity, lv -> {
                lv.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f);
                ZeroContactLogger.LOG.info(lv.getName() + "的插板碎掉了！");
            });
        });
    }

    private static float getDurabilityLossFactor(float amount, float durabilityLossFactor) {
        switch (ProtectionLevelHelper.get((int) Math.floor(amount))) {
            case NIJIIA -> durabilityLossFactor = 0.1F;
            case NIJII -> durabilityLossFactor = 0.4F;
            case NIJIIIA -> durabilityLossFactor = 0.7F;
            case NIJIII -> durabilityLossFactor = 1.1F;
            case NIJIV -> durabilityLossFactor = 1.5F;
        }
        return PlateEntityHurtEvent.isHeadShot?durabilityLossFactor*1.5f:durabilityLossFactor;
    }

    public static void DamageHelmet(EntityHurtByGunEvent event) {
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        float amount = event.getAmount();
        if (PlateEntityHurtEvent.isHeadShot) {
            entity.ifPresent(e -> {
                if (e instanceof LivingEntity livingEntity) {
                    ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                    float durabilityLossFactor = 1;
                    int hits = stack.getOrCreateTag().getInt("hits") + 1;
                    int durabilityLossAmount = 1;
                    if (!(stack.getItem() instanceof HelmetInfoProvider &&stack.getItem() instanceof DurabilityLossProvider durabilityLossProvider))
                        return;
                    durabilityLossFactor = getDurabilityLossFactor(durabilityLossAmount,durabilityLossFactor);
                    durabilityLossAmount = durabilityLossProvider.generateLoss(amount,durabilityLossFactor,hits);
                    stack.getOrCreateTag().putInt("hits", hits);
                    stack.hurtAndBreak(durabilityLossAmount,livingEntity,broken->{
                        livingEntity.playSound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, 1.0f, 1.0f);
                    });
                }
            });
        }
    }

    public static void DamagePlateRegister(LivingEntity entity, DamageSource damageSource, float amount) {
        if (EventUtil.isDamageSourceValid(damageSource)) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
                DamagePlateModifier(inv, entity, damageSource, amount, EventUtil.idHitFromBack(entity, damageSource));
            });
        }
    }
}
