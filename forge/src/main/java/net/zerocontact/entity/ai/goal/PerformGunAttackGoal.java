package net.zerocontact.entity.ai.goal;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

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
        burstFire();
    }

    public static <T extends ArmedRaider>boolean canSee(T shooter) {
        LivingEntity target = shooter.getTarget();
        if (target == null) return false;
        Vec3 lookVec = shooter.getLookAngle().normalize();
        Vec3 toTargetVec = target.position().subtract(shooter.position()).normalize();
        double dot = lookVec.dot(toTargetVec);
        double fovCos = Math.cos(Math.toRadians(90));
        boolean bodyFacing = shooter.yBodyRot == shooter.yHeadRot;
        if ((dot >= fovCos || shooter.isSprinting()) && dot >= fovCos && bodyFacing) {
            shooter.stateController.getShareContext().canSeeTarget =true;
            return true;
        }
        shooter.stateController.getShareContext().canSeeTarget =false;
        return false;
    }

    private float provideInaccuracy() {
        float baseSpread = 5;
        if (random.nextFloat() < .1F) {
            return (float) random.triangle(baseSpread / 2, baseSpread);
        }
        return (float) random.triangle(baseSpread, baseSpread * 1.25F);
    }

    private void shoot(LivingEntity target) {
        double x, y, z;
        Vec3 cacheTarget = shooter.stateController.getShareContext().cacheTarget;
        if (target == null || cacheTarget != null) {
            x = cacheTarget.x() - shooter.getX();
            y = cacheTarget.y() - shooter.getEyeY();
            z = cacheTarget.z() - shooter.getZ();
        } else {
            x = target.getX() - shooter.getX();
            y = target.getEyeY() - shooter.getEyeY();
            z = target.getZ() - shooter.getZ();
        }
        float spread = provideInaccuracy();
        float yaw = (float) -Math.toDegrees(Math.atan2(x, z)) + Mth.randomBetween(random, -spread, spread);
        float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z))) + Mth.randomBetween(random, -spread, spread);
        ShootResult result = operator.shoot(() -> pitch, () -> yaw);
        if (result == ShootResult.NOT_DRAW) {
            operator.draw(shooter::getMainHandItem);
            return;
        }
        if (result == ShootResult.NO_AMMO) {
            operator.reload();
        }

    }

    private void burstFire() {
        if (!IGun.mainhandHoldGun(shooter) || operator == null) return;
        if (!shooter.getNavigation().isDone()) return;
        LivingEntity target = shooter.getTarget();
        Vec3 cacheTarget = shooter.stateController.getShareContext().cacheTarget;
        if (target != null) {
            shooter.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        } else if (cacheTarget != null) {
            shooter.lookAt(EntityAnchorArgument.Anchor.EYES, cacheTarget);
        } else {
            return;
        }
        if (shootCoolDown > 0) {
            shootCoolDown--;
            FireMode fireMode = IGun.getMainhandFireMode(shooter);
            if (fireMode == FireMode.SEMI || fireMode == FireMode.BURST) {
                burstInterval = random.nextInt(10);
            } else {
                burstInterval = random.nextInt(15);
            }
        } else {
            if (burstInterval > 0 && canSee(shooter)) {
                shoot(target);
                burstInterval--;
            } else {
                shootCoolDown = 40;
                burstInterval = random.nextInt(15);
            }
        }
    }
}
