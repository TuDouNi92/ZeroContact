package net.zerocontact.entity.ai.goal;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;

public class PerformGunAttackGoal extends Goal {
    private final Mob shooter;
    private final int coolDownTicks;
    private int coolDown = 0;
    private int shootCoolDown = 0;
    private int burstInterval = 0;
    private final RandomSource random;
    private final IGunOperator operator;

    public PerformGunAttackGoal(Mob shooter, int coolDownTicks) {
        this.shooter = shooter;
        this.coolDownTicks = coolDownTicks;
        this.operator = IGunOperator.fromLivingEntity(shooter);
        this.random = shooter.getRandom();
    }

    @Override
    public boolean canUse() {
        return shooter.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        shooter.getTarget();
        boolean isHurt = false;
        if (shooter.getTarget() != null) {
            ArmedRaider raider = (ArmedRaider) shooter;
            isHurt = raider.isHurt;
        }
        return coolDown == 0 || !isHurt;
    }

    @Override
    public void start() {
        coolDown = coolDownTicks;
    }

    @Override
    public void tick() {
        burstFire();
        coolDown--;
    }

    private boolean canSee(LivingEntity target) {
        Vec3 lookVec = shooter.getLookAngle().normalize();
        Vec3 toTargetVec = target.position().subtract(shooter.position()).normalize();
        double dot = lookVec.dot(toTargetVec);
        double fovCos = Math.cos(Math.toRadians(90));
        return (dot >= fovCos || shooter.isSprinting()) && dot >= fovCos;
    }

    private float provideInaccuracy() {
        float baseSpread = 5;
        if (random.nextFloat() < .1F) {
            return (float) random.triangle(baseSpread / 2, 2);
        }
        return (float) random.triangle(baseSpread, 1);
    }

    private void shoot(LivingEntity target) {
        double x = target.getX() - shooter.getX();
        double y = target.getEyeY() - shooter.getEyeY();
        double z = target.getZ() - shooter.getZ();
        float spread = provideInaccuracy();
        float yaw = (float) -Math.toDegrees(Math.atan2(x, z)) + Mth.randomBetween(random, -spread, spread);
        float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z))) + Mth.randomBetween(random, -spread, spread);
        if (!IGun.mainhandHoldGun(shooter) || operator == null) return;
        if (!canSee(target)) return;
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
        LivingEntity target = shooter.getTarget();
        if (target != null) {
            if (shootCoolDown > 0) {
                shootCoolDown--;
                FireMode fireMode = IGun.getMainhandFireMode(shooter);
                if (fireMode == FireMode.SEMI || fireMode == FireMode.BURST) {
                    burstInterval = random.nextInt(10);
                } else {
                    burstInterval = random.nextInt(15);
                }
            } else {
                if (burstInterval > 0) {
                    shoot(shooter.getTarget());
                    burstInterval--;
                } else {
                    shootCoolDown = 40;
                    burstInterval = random.nextInt(15);
                }
            }
        }
    }
}
