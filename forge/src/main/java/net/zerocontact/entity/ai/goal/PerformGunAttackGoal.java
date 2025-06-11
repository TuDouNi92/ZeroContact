package net.zerocontact.entity.ai.goal;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.ZeroContactLogger;

public class PerformGunAttackGoal extends Goal {
    private final Mob mob;
    private final int coolDownTicks;
    private int coolDown = 0;
    private int shootCoolDown = 0;
    private int burstInterval = 0;
    private final RandomSource random;
    private final IGunOperator operator;

    public PerformGunAttackGoal(Mob mob, int coolDownTicks) {
        this.mob = mob;
        this.coolDownTicks = coolDownTicks;
        this.operator = IGunOperator.fromLivingEntity(mob);
        this.random = mob.getRandom();
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return coolDown == 0;
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
        Vec3 lookVec = mob.getLookAngle().normalize();
        Vec3 toTargetVec = target.position().subtract(mob.position()).normalize();
        double dot = lookVec.dot(toTargetVec);
        double fovCos = Math.cos(Math.toRadians(90));
        return (dot >= fovCos || mob.isSprinting()) && dot >= fovCos;
    }

    private void shoot(LivingEntity target) {
        double x = target.getX() - mob.getX();
        double y = target.getEyeY() - mob.getEyeY();
        double z = target.getZ() - mob.getZ();
        float spread = 5;
        float yaw = (float) -Math.toDegrees(Math.atan2(x, z)) + Mth.randomBetween(random, -spread, spread);
        float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z))) + Mth.randomBetween(random, -spread, spread);
        if (!IGun.mainhandHoldGun(mob) || operator == null) return;
        if (!canSee(target)) return;
        ShootResult result = operator.shoot(() -> pitch, () -> yaw);
        ZeroContactLogger.LOG.info(result);
        if (result == ShootResult.NOT_DRAW) {
            operator.draw(mob::getMainHandItem);
            return;
        }
        if (result == ShootResult.NO_AMMO) {
            operator.reload();
        }
    }

    private void burstFire() {
        if (mob.getTarget() != null) {
            if (shootCoolDown > 0) {
                shootCoolDown--;
                burstInterval = random.nextInt(15);
            } else {
                if (burstInterval > 0) {

                    shoot(mob.getTarget());
                    burstInterval--;
                } else {
                    shootCoolDown = 40;
                    burstInterval = random.nextInt(40);
                }
            }
        }
    }
}
