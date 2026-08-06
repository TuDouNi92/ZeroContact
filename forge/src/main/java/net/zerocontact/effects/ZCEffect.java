package net.zerocontact.effects;

import net.minecraft.server.level.ServerLevel;
import net.zerocontact.caliber.HookEffectInvocation;
import net.zerocontact.forge_registries.ZCEffects;

public interface ZCEffect {
    void instantEffect(HookEffectInvocation hookEffectInvocation);

    void serverTickEffect(ServerLevel level);

    class Tick {
        public static void serverTick(ServerLevel level) {
            ZCEffects.EFFECT_DEFERRED_REGISTER.forEach(effect -> {
                if (effect.get() instanceof ZCEffect zcEffect) {
                    zcEffect.serverTickEffect(level);
                }
            });
        }
    }
}
