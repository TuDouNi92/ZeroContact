package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

import static net.zerocontact.ZeroContact.MOD_ID;

public final class ZCParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> PERSISTENT_SIGNAL_SMOKE =
            PARTICLE_TYPES.register("persistent_signal_smoke", () -> new SimpleParticleType(false));

    private ZCParticles() {
    }

    public static void register() {
        PARTICLE_TYPES.register();
    }
}
