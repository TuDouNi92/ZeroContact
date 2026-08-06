package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.zerocontact.effects.IgnitionEffect;
import net.zerocontact.effects.SmokeEffect;


import static net.zerocontact.ZeroContact.MOD_ID;

public class ZCEffects {
    public static final DeferredRegister<MobEffect> EFFECT_DEFERRED_REGISTER = DeferredRegister.create(MOD_ID, Registries.MOB_EFFECT);
    public static final RegistrySupplier<MobEffect> IGNITION = EFFECT_DEFERRED_REGISTER.register("ignition", IgnitionEffect::new);
    public static final RegistrySupplier<MobEffect> SMOKE = EFFECT_DEFERRED_REGISTER.register("smoke", SmokeEffect::new);

    public static void register() {
        EFFECT_DEFERRED_REGISTER.register();
    }
}
