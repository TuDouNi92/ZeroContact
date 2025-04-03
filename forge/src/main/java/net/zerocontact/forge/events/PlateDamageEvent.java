package net.zerocontact.forge.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ModLogger;
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
            //记录受击数
            int hits = stack.getOrCreateTag().getInt("hitCount") + 1;

            //记录插板耐久损耗乘数
            int damagePlateMultiplier = Math.max(1, stack.getOrCreateTag().getInt("damage_plate_multiplier"));

            //记录插板最大承受伤害，随着受击次数非线性递减
            int maxDamageCanHold = 12;
            int maxDamageCanHoldCapacity = Math.max(0, maxDamageCanHold-(int) Math.floor(Math.pow(hits,3)/4));

            //sourceTypeFactor：伤害来源的类型决定耐久消耗速率高低
            float sourceTypeFactor = damageSource.is(DamageTypes.MOB_ATTACK) ? 0.1f : 1;
            int durabilityLossCurve = (int) Math.ceil(damagePlateMultiplier * sourceTypeFactor * Math.pow(hits, 1.5));
            stack.getOrCreateTag().putInt("hitCount", hits);
            stack.hurtAndBreak(durabilityLossCurve, entity, livingEntity -> {
                ModLogger.LOG.info(livingEntity.getName()+"的插板碎掉了！");
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
