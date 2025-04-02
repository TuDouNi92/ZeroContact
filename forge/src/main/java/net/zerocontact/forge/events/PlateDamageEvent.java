package net.zerocontact.forge.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class DamagePlateEvent {

    private static void DamagePlateModifier(
            ICuriosItemHandler iCuriosItemHandler,
            LivingEntity entity,
            DamageSource damageSource,
            float amount,
            String identifier
    ) {
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            int hits = stack.getOrCreateTag().getInt("hitCount") + 1;
            int damagePlateMultiplier = Math.max(1, stack.getOrCreateTag().getInt("damage_plate_multiplier"));

            //soruceTypeFactor：伤害来源的类型决定耐久消耗速率高低
            float sourceTypeFactor = damageSource.is(DamageTypes.MOB_ATTACK) ? 0.1f : 1;
            int durabilityLossCurve = (int) Math.ceil(damagePlateMultiplier * sourceTypeFactor * Math.pow(hits, 1.5));
            stack.getOrCreateTag().putInt("hitCount", hits);
            stack.hurtAndBreak(durabilityLossCurve, entity, livingEntity -> {

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
