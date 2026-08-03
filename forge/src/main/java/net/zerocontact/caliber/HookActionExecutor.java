package net.zerocontact.caliber;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.stream.Stream;

public final class HookActionExecutor {
    public static void execute(HookActionData actionData, HookContext context) {
        Stream<LivingEntity> livingEntityStream = actionData.target().resolve(context, actionData.radius());
        livingEntityStream.forEach(lv ->
                Optional.ofNullable(actionData.resolveEffect())
                        .ifPresent(
                                mobEffect -> lv.addEffect(new MobEffectInstance(
                                        mobEffect,
                                        actionData.duration(),
                                        actionData.amplifier()
                                ))));

    }
}
