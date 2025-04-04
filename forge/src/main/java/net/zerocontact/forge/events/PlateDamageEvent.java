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
            int durabilityLossAmount;
            //记录受击数
            int hits = stack.getOrCreateTag().getInt("hitCount") + 1;
            if (!stack.isEmpty() && damageSource.is(ModDamageTypes.BULLET)) {
                stack.getOrCreateTag().putInt("hitCount", hits);
                if (amount < SapiIV.MAX_HURT_DAMAGE_CAN_HOLD) {
                    //钝伤
                    durabilityLossAmount = (int) Math.floor(Math.pow(hits*0.2f, 3));
                    ModLogger.LOG.info("blunt damage:" + durabilityLossAmount);
                } else {
                    //贯穿
                    durabilityLossAmount = (int) Math.floor(Math.pow(hits*0.6f, 3));
                    ModLogger.LOG.info("penetrate damage:" + durabilityLossAmount);
                }

            }
            else{
                durabilityLossAmount = 1;
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
