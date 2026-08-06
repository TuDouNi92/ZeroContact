package net.zerocontact.caliber;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.datagen.AmmoDataPOJO;
import net.zerocontact.effects.ZCEffect;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class HookActionExecutor {
    private static final RandomSource random = RandomSource.create();

    public static void execute(AmmoDataPOJO.HookActionData actionData, HookContext context) {
        if (random.nextFloat() > actionData.chance()) return;
        Stream<LivingEntity> livingEntityStream = actionData.target().resolve(context, actionData.radius());
        Optional<MobEffect> mobEffect = Optional.ofNullable(actionData.resolveEffect());
        if (mobEffect.isEmpty()) return;
        List<LivingEntity> targets = livingEntityStream.toList();
        if (mobEffect.get() instanceof ZCEffect zcEffect) {
            zcEffect.instantEffect(new HookEffectInvocation(actionData, context, targets));
        }
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(
                    mobEffect.get(),
                    actionData.duration(),
                    actionData.amplifier())
            );
        }
    }
}
