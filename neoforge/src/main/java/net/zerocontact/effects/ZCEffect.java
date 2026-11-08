package net.zerocontact.effects;

import net.minecraft.server.level.ServerLevel;
import net.zerocontact.caliber.extension.model.HookEffectInvocation;
import net.zerocontact.forge_registries.EffectRegistry;

public interface ZCEffect {
    void instantEffect(HookEffectInvocation hookEffectInvocation);

    void serverTickEffect(ServerLevel level);

    class Tick {
        public static void serverTick(ServerLevel level) {
            EffectRegistry.EFFECT_DEFERRED_REGISTER.forEach(effect -> {
                if (effect.get() instanceof ZCEffect zcEffect) {
                    zcEffect.serverTickEffect(level);
                }
            });
        }
    }
}
