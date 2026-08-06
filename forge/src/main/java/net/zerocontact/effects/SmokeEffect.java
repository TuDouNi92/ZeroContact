package net.zerocontact.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.caliber.HookContext;
import net.zerocontact.caliber.HookEffectInvocation;
import net.zerocontact.datagen.AmmoDataPOJO;
import net.zerocontact.forge_registries.ZCParticles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SmokeEffect extends MobEffect implements ZCEffect {
    private static final int EMISSION_INTERVAL_TICKS = 3;
    private static final double CLOUD_MERGE_DISTANCE = 4D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double RADIAL_SEQUENCE = 0.7548776662466927D;
    private static final int PARTICLES_PER_SAMPLE = 2;
    private static final Map<ServerLevel, List<SmokeCloud>> ACTIVE_CLOUDS = new WeakHashMap<>();

    public SmokeEffect() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    @Override
    public void instantEffect(HookEffectInvocation hookEffectInvocation) {
        HookContext context = hookEffectInvocation.context();
        AmmoDataPOJO.HookActionData data = hookEffectInvocation.data();
        LivingEntity entity = context.shooter();
        if (entity == null) return;
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 position = context.positon();
        BlockPos blockPos = BlockPos.containing(position.x, position.y, position.z);
        createSmokeCloud(serverLevel, blockPos.getCenter(), data.radius(), data.duration());

    }

    private void applyBlindness(ServerLevel serverLevel, Vec3 center, float radius) {
        AABB box = new AABB(BlockPos.containing(center)).inflate(radius);
        serverLevel.getEntitiesOfClass(LivingEntity.class, box)
                .forEach(
                        lv -> lv.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30))
                );
    }

    private static void createSmokeCloud(
            ServerLevel level,
            Vec3 center,
            float requestedRadius,
            int duration
    ) {
        float radius = Mth.clamp(requestedRadius, 0.5F, 32.0F);
        int durationTicks = Math.max(1, duration);
        List<SmokeCloud> clouds = ACTIVE_CLOUDS.computeIfAbsent(level, ignored -> new ArrayList<>());

        for (SmokeCloud cloud : clouds) {
            if (cloud.center.distanceToSqr(center) <= CLOUD_MERGE_DISTANCE * CLOUD_MERGE_DISTANCE) {
                cloud.radius = Math.max(cloud.radius, radius);
                cloud.remainingTicks = Math.max(cloud.remainingTicks, durationTicks);
                spawnSmokeParticles(level, center, radius, 1);
                return;
            }
        }

        spawnSmokeParticles(level, center, radius, 2);
        clouds.add(new SmokeCloud(level, center, radius, durationTicks));
    }

    public void serverTickEffect(ServerLevel level) {
        List<SmokeCloud> clouds = ACTIVE_CLOUDS.get(level);
        if (clouds == null) return;

        clouds.removeIf(cloud -> {
            cloud.remainingTicks--;
            if (cloud.remainingTicks <= 0) return true;
            if (cloud.remainingTicks % EMISSION_INTERVAL_TICKS == 0) {
                spawnSmokeParticles(cloud.level, cloud.center, cloud.radius, 1);
            }
            applyBlindness(level, cloud.center, cloud.radius*0.45f);
            return false;
        });

        if (clouds.isEmpty()) {
            ACTIVE_CLOUDS.remove(level);
        }
    }

    private static void spawnSmokeParticles(ServerLevel level, Vec3 center, float radius, int densityMultiplier) {
        int particleCount = Mth.clamp(Mth.ceil(2.0F * radius), 32, 96) * densityMultiplier;
        int sampleCount = Mth.ceil((float) particleCount / PARTICLES_PER_SAMPLE);
        double cloudRadius = Math.max(0.5D, radius * 0.45D);
        double localJitter = Math.max(0.08D, cloudRadius * 0.04D);
        double rotation = level.random.nextDouble() * Mth.TWO_PI;
        double centerY = center.y + radius * 0.12D;

        for (int i = 0; i < sampleCount; i++) {
            double y = 1.0D - 2.0D * (i + 0.5D) / sampleCount;
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double angle = GOLDEN_ANGLE * i + rotation;

            // Fibonacci directions plus a low-discrepancy radial sequence fill the sphere evenly.
            double radialFraction = (i * RADIAL_SEQUENCE + 0.5D / sampleCount) % 1.0D;
            double distance = cloudRadius * Math.cbrt(radialFraction);
            double x = Math.cos(angle) * horizontal * distance;
            double z = Math.sin(angle) * horizontal * distance;
            int particlesAtSample = Math.min(PARTICLES_PER_SAMPLE, particleCount - i * PARTICLES_PER_SAMPLE);

            level.sendParticles(
                    ZCParticles.PERSISTENT_SIGNAL_SMOKE.get(),
                    center.x + x,
                    centerY + y * distance,
                    center.z + z,
                    particlesAtSample,
                    localJitter,
                    localJitter,
                    localJitter,
                    0.008D
            );
        }
    }

    private static final class SmokeCloud {
        private final ServerLevel level;
        private final Vec3 center;
        private float radius;
        private int remainingTicks;

        private SmokeCloud(ServerLevel level, Vec3 center, float radius, int duration) {
            this.level = level;
            this.center = center;
            this.radius = radius;
            remainingTicks = duration;
        }
    }
}
