package net.zerocontact.suppression;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Consumer;

public class BulletSuppression {
    public static void apply(Entity bullet, LivingEntity entity, Consumer<Entity> consumer) {
        if (entity == null) return;
        if (bullet instanceof EntityKineticBullet) {
            Optional<Entity> bulletEntity = Optional.of(bullet);
            bulletEntity.ifPresent(bl -> {
                Vec3 targetPos = entity.getEyePosition();
                Vec3 relativePos = bullet.position().subtract(targetPos);
                Vec3 relativeVelocity = bullet.getDeltaMovement().subtract(entity.getDeltaMovement());
                double speedSqr = relativeVelocity.lengthSqr();
                if (speedSqr < 1.0E-8) {
                    return;
                }
                double closestTime = -relativePos.dot(relativeVelocity) / speedSqr;
                if (closestTime < 0.0 || closestTime > 1.0) {
                    return;
                }
                Vec3 closestOffset = relativePos.add(
                        relativeVelocity.scale(closestTime)
                );
                double closestDistance = closestOffset.length();

                if (closestDistance >= 0.5 && closestDistance <= 4) {
                    consumer.accept(bl);
                }
            });
        }
    }
}
