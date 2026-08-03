package net.zerocontact.client.tooltip;

import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import net.zerocontact.caliber.CaliberVariantDamageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable, horizontal-shot trajectory used by the ammo tooltip.
 *
 * <p>The update order mirrors {@code EntityKineticBullet#tick}: move with the
 * current velocity, apply air friction, then apply gravity.</p>
 */
final class BallisticTrajectory {
    private static final int MAX_SIMULATION_TICKS = 1200;
    private static final double MIN_SCALE = 1.0E-4;
    private static final double DISPLAY_DISTANCE = 200.0;
    private static final double DISPLAY_DROP = 10.0;

    private final List<Sample> samples;
    private final double maxDistance;
    private final double maxDrop;

    private BallisticTrajectory(List<Sample> samples, double maxDistance, double maxDrop) {
        this.samples = List.copyOf(samples);
        this.maxDistance = maxDistance;
        this.maxDrop = maxDrop;
    }

    static BallisticTrajectory simulate(CaliberVariantDamageHelper.Caliber caliber, IGunOperator iGunOperator) {
        AttachmentCacheProperty property = iGunOperator.getCacheProperty();
        double ammoSpeed = 0.0;
        if (property != null) {
            Float cachedAmmoSpeed = property.getCache(GunProperties.AMMO_SPEED);
            if (cachedAmmoSpeed != null) {
                ammoSpeed = cachedAmmoSpeed
                        * AmmoConfig.GLOBAL_BULLET_SPEED_MODIFIER.get()
                        / 20.0;
            }
        }
        int ticks = Math.min(Math.max(caliber.life(), 1), MAX_SIMULATION_TICKS);
        double horizontalVelocity = Math.max(ammoSpeed, 0.0);
        double verticalVelocity = 0.0;
        double gravity = Math.max(caliber.gravity(), 0.0F);
        double dragMultiplier = 1.0 - clamp(caliber.friction());
        double distance = 0.0;
        double height = 0.0;

        List<Sample> samples = new ArrayList<>(ticks + 1);
        samples.add(new Sample(0.0, 0.0));

        for (int tick = 0; tick < ticks; tick++) {
            if (horizontalVelocity <= MIN_SCALE) {
                break;
            }

            double nextDistance = distance + horizontalVelocity;
            double nextHeight = height + verticalVelocity;
            double boundaryFraction = 1.0;
            boolean reachedBoundary = false;

            if (nextDistance >= DISPLAY_DISTANCE) {
                boundaryFraction = Math.min(
                        boundaryFraction,
                        (DISPLAY_DISTANCE - distance) / (nextDistance - distance)
                );
                reachedBoundary = true;
            }

            if (nextHeight <= -DISPLAY_DROP && nextHeight < height) {
                boundaryFraction = Math.min(
                        boundaryFraction,
                        (-DISPLAY_DROP - height) / (nextHeight - height)
                );
                reachedBoundary = true;
            }

            if (reachedBoundary) {
                samples.add(new Sample(
                        distance + (nextDistance - distance) * boundaryFraction,
                        height + (nextHeight - height) * boundaryFraction
                ));
                break;
            }

            distance = nextDistance;
            height = nextHeight;
            samples.add(new Sample(distance, height));

            horizontalVelocity *= dragMultiplier;
            verticalVelocity *= dragMultiplier;
            verticalVelocity -= gravity;
        }

        return new BallisticTrajectory(
                samples,
                DISPLAY_DISTANCE,
                DISPLAY_DROP
        );
    }

    List<Sample> samples() {
        return samples;
    }

    double maxDistance() {
        return maxDistance;
    }

    double maxDrop() {
        return maxDrop;
    }

    private static float clamp(float value) {
        return Math.max((float) 0.0, Math.min((float) 1.0, value));
    }

    record Sample(double distance, double height) {
    }
}
