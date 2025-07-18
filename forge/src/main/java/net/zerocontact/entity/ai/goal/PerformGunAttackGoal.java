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
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.Objects;
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
        if(!shooter.stateController.getShareContext().isHurt){
            burstFire();
        }
        else{
            burstFireWithExtract();
        }
    }

    public static <T extends ArmedRaider> boolean canSee(T shooter) {
        LivingEntity target = shooter.getTarget();
        if (target == null) return false;
        Vec3 lookVec = shooter.getLookAngle().normalize();
        Vec3 toTargetVec = target.position().subtract(shooter.position()).normalize();
        double dot = lookVec.dot(toTargetVec);
        double fovCos = Math.cos(Math.toRadians(120));
        boolean bodyFacing = shooter.yBodyRot == shooter.yHeadRot;
        return (dot >= fovCos || shooter.isSprinting()) && dot >= fovCos && bodyFacing;
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
        x = target.getX() - shooter.getX();
        double targetY = target.getY()+target.getBbHeight()*0.5;
        y = targetY - shooter.getEyeY();
        z = target.getZ() - shooter.getZ();
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
        if (target != null) {
            shooter.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
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

    private void burstFireWithExtract() {
        burstFire();
        if(shooter.getTarget()!=null){
            Vec3 targetPos =LandRandomPos.getPosAway(shooter,12,6, shooter.getTarget().position());
            Optional.ofNullable(targetPos).ifPresent(__-> shooter.getNavigation().moveTo(targetPos.x,targetPos.y,targetPos.z,1.25D));
        }
    }
}
