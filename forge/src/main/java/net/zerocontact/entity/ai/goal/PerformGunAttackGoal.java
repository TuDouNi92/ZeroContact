package net.zerocontact.entity.ai.goal;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.Optional;

public class PerformGunAttackGoal extends Goal {
    private final ArmedRaider shooter;
    private int shootCoolDown = 0;
    private int burstInterval = 0;
    private final RandomSource random;
    private final IGunOperator operator;

    public PerformGunAttackGoal(ArmedRaider shooter) {
        this.shooter = shooter;
        this.operator = IGunOperator.fromLivingEntity(shooter);
        this.random = shooter.getRandom();
    }

    @Override
    public boolean canUse() {
        return shooter.stateController.getPhase() == GlobalStateController.Phase.ATTACK;
    }

    @Override
    public void tick() {
        if (!shooter.stateController.getShareContext().isHurt) {
            burstFire();
        } else {
            burstFireWithFallBack();
        }
    }

    public static <T extends Mob> boolean isInVisionToShoot(T shooter) {
        LivingEntity target = shooter.getTarget();
        if (target == null) return false;
        return isInVisionToShoot(shooter, target);
    }


    public static <T extends Mob> boolean isInVisionToShoot(T shooter, LivingEntity target) {
        if (target == null) return false;
        Vec3 lookVec = shooter.getLookAngle().normalize();
        Vec3 toTargetVec = target.position().subtract(shooter.position()).normalize();
        double dot = lookVec.dot(toTargetVec);
        double fovCos = Math.cos(Math.toRadians(110));
        return dot >= fovCos && shooter.hasLineOfSight(target);
    }

    private float provideInaccuracy() {
        float baseSpread = 5;
        if (random.nextFloat() < .1F) {
            return (float) random.triangle(baseSpread / 2, baseSpread);
        }
        return (float) random.triangle(baseSpread, baseSpread * 1.25F);
    }

    private ShootResult shoot(LivingEntity target) {
        ShootResult result = ShootResult.UNKNOWN_FAIL;
        IGun gun = IGun.getIGunOrNull(shooter.getMainHandItem());
        if (gun == null) return result;
        double x, y, z;
        x = target.getX() - shooter.getX();
        double targetY = target.getY() + target.getBbHeight() * 0.5;
        y = targetY - shooter.getEyeY();
        z = target.getZ() - shooter.getZ();
        float spread = provideInaccuracy();
        float yaw = (float) -Math.toDegrees(Math.atan2(x, z)) + Mth.randomBetween(random, -spread, spread);
        float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z))) + Mth.randomBetween(random, -spread, spread);
        result = operator.shoot(() -> pitch, () -> yaw);
        return result;
    }

    private static double getWantedY(Entity entity) {
        return entity instanceof LivingEntity ? entity.getEyeY() : (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / (double) 2.0F;
    }

    private void burstFire() {
        if (!IGun.mainHandHoldGun(shooter) || operator == null) return;
        LivingEntity target = shooter.getTarget();
        if (target != null) {
            shooter.getLookControl().setLookAt(target.getX(), getWantedY(target), target.getZ(), 72f, 36f);
        } else {
            return;
        }
        if (shootCoolDown > 0) {
            shootCoolDown--;
            FireMode fireMode = IGun.getMainHandFireMode(shooter);
            if (fireMode == FireMode.SEMI || fireMode == FireMode.BURST) {
                burstInterval = random.nextInt(20);
            } else {
                burstInterval = random.nextInt(15);
            }
        } else {
            if (burstInterval > 0 && isInVisionToShoot(shooter) && shooter.getLookControl().isLookingAtTarget()) {
                if (operator.getSynReloadState().getStateType().isReloading()) return;
                shooter.getNavigation().stop();
                operator.aim(shooter.distanceTo(target) > 20.0f);
                ShootResult result = shoot(target);
                if (result != null) {
                    switch (result) {
                        case NEED_BOLT -> operator.bolt();
                        case NO_AMMO -> operator.reload();
                        case NOT_DRAW -> operator.draw(shooter::getMainHandItem);
                    }
                }
                burstInterval--;
            } else {
                shootCoolDown = 10;
                burstInterval = random.nextInt(15);
            }
        }
    }

    private void burstFireWithFallBack() {
        burstFire();
        if (shooter.getTarget() != null) {
            Vec3 targetPos = LandRandomPos.getPosAway(shooter, 12, 6, shooter.getTarget().position());
            Optional.ofNullable(targetPos).ifPresent(__ -> shooter.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.25D));
        }
    }
}
