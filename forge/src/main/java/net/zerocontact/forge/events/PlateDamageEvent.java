package net.zerocontact.forge.events;

import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ModLogger;
import net.zerocontact.SapiIV;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class PlateDamageEvent {

    private static void DamagePlateModifier(
            ICuriosItemHandler iCuriosItemHandler,
            LivingEntity entity,
            DamageSource damageSource,
            float amount,
            String identifier
    ) {
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            float durabilityLossFactor = 1;
            int durabilityLossAmount = 1;
            if (!stack.isEmpty() && damageSource.is(ModDamageTypes.BULLET)) {
                if(amount<4){
                    durabilityLossFactor=0.1f;
                }
                else if(amount<9){
                    durabilityLossFactor=0.7f;
                }
                else if(amount<13){
                    durabilityLossFactor=0.95f;
                }
                durabilityLossAmount =(int) Math.floor(durabilityLossFactor*amount);
            }
            stack.hurtAndBreak(durabilityLossAmount, entity, livingEntity -> {
                ModLogger.LOG.info(livingEntity.getName() + "的插板碎掉了！");
            });
        });
    }

    public static void DamagePlateRegister(LivingEntity entity, DamageSource damageSource, float amount) {
        if (entity instanceof Player && EventUtil.isDamageSourceValid(damageSource)) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
                DamagePlateModifier(inv, entity, damageSource, amount, "front_plate");
                DamagePlateModifier(inv, entity, damageSource, amount, "back_plate");
            });
        }
    }
}
