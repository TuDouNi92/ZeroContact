package net.zerocontact.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public final class PersistentSignalSmokeParticle extends TextureSheetParticle {
    private PersistentSignalSmokeParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ
    ) {
        super(level, x, y, z);
        scale(3.0F);
        setSize(0.25F, 0.25F);
        lifetime = random.nextInt(50) + 280;
        gravity = 3.0E-6F;
        xd = velocityX;
        yd = velocityY + random.nextFloat() / 500.0F;
        zd = velocityZ;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;

        if (age++ >= lifetime) {
            remove();
            return;
        }

        xd += random.nextFloat() / 5000.0F * (random.nextBoolean() ? 1 : -1);
        zd += random.nextFloat() / 5000.0F * (random.nextBoolean() ? 1 : -1);
        yd -= gravity;
        move(xd, yd, zd);

    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                @NotNull SimpleParticleType type,
                @NotNull ClientLevel level,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ
        ) {
            PersistentSignalSmokeParticle particle = new PersistentSignalSmokeParticle(
                    level, x, y, z, velocityX, velocityY, velocityZ
            );
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
