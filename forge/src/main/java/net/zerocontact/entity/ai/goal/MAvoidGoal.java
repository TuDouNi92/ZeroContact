package net.zerocontact.entity.ai.goal;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;

public class MAvoidGoal extends Goal {
    private final ArmedRaider mob;
    private final PerformGunAttackGoal performGunAttackGoal;
    private final int distance;
    private final RandomSource random;
    private int shootBackCooldown = 0;
    private int interval = 0;
    private float randomInterval = 0.5F;
    private float cacheMoveX = 0, cacheMoveZ = 0;
    private static final float strafeDelta = 1.5F;

    public MAvoidGoal(ArmedRaider mob, int distance, PerformGunAttackGoal performGunAttackGoal) {
        this.mob = mob;
        this.distance = distance;
        this.random = mob.getRandom();
        this.performGunAttackGoal = performGunAttackGoal;
    }

    @Override
    public boolean canUse() {
        return mob.isHurt;
    }

    @Override
    public void tick() {
        if (interval == 20) {
            randomInterval = random.nextFloat();
            interval = 0;
        } else {
            interval++;
        }
        if (randomInterval <= 0.5F) {
            shootBackStrafe();
        } else {
            runAway();
        }
    }

    private void runAway() {
        if (!mob.getNavigation().isDone() || shootBackCooldown != 0) return;
        int targetX = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
        int targetZ = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
        Vec3 targetPos = mob.position().add(targetX, 0, targetZ);
        mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
    }

    private void shootBackStrafe() {
        if (mob.getTarget() == null || !performGunAttackGoal.canSee() || !mob.getNavigation().isDone()) runAway();
        else {
            if (cacheMoveX == 0 && cacheMoveZ == 0) {
                cacheMoveX = random.nextBoolean() ? strafeDelta : -strafeDelta;
                cacheMoveZ = random.nextBoolean() ? strafeDelta : -strafeDelta;
            }
            if (shootBackCooldown == 40) {
                cacheMoveX = random.nextBoolean() ? strafeDelta : -strafeDelta;
                cacheMoveZ = random.nextBoolean() ? strafeDelta : -strafeDelta;
                shootBackCooldown = 0;
            } else {
                shootBackCooldown++;
                mob.getMoveControl().strafe(cacheMoveX, cacheMoveZ);
            }
        }
    }
}
