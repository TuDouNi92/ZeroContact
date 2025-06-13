package net.zerocontact.client.interaction;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.Optional;

public class BulletPassBy {
    public static void playBulletPassBySound(Entity bullet, LivingEntity entity) {
        if (bullet instanceof EntityKineticBullet) {
            Optional<Entity> bulletEntity = Optional.of(bullet);
            bulletEntity.ifPresent(bl -> {
                Vec3 direction = bl.getDeltaMovement().normalize();
                Vec3 toTarget = entity.position().subtract(bl.position()).normalize();
                double dot = direction.dot(toTarget);
                if (bl.distanceTo(entity) <= 16 && dot>0.4) {
                    bl.playSound(ModSoundEventsReg.randomBulletSound());
                }
            });
        }
    }
}
