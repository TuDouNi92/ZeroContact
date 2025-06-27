package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.Objects;

public class TailGoal extends Goal {
    private final ArmedRaider raider;
    private int intervalCooldown;

    public TailGoal(ArmedRaider mob) {
        this.raider = mob;
    }

    @Override
    public boolean canUse() {
        return raider.stateController.getPhase() == GlobalStateController.Phase.CHASE;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        chase();
    }

    public static <T extends ArmedRaider> boolean canChaseTarget(T raider) {
        boolean canSee = PerformGunAttackGoal.canSee(raider);
        if (canSee) {
            raider.stateController.getShareContext().cacheTarget = Objects.requireNonNull(raider.getTarget()).position();
            raider.stateController.getShareContext().canChaseTarget = false;
            return false;
        }
        if (raider.stateController.getShareContext().cacheTarget != null) {
            raider.stateController.getShareContext().canChaseTarget = true;
            return true;
        }
        return false;
    }

    private void chase() {
        Vec3 cacheTargetPos = raider.stateController.getShareContext().cacheTarget;
        if (intervalCooldown == 40 && raider.getNavigation().isDone()) {
            intervalCooldown = 0;
            raider.getNavigation().moveTo(cacheTargetPos.x, cacheTargetPos.y, cacheTargetPos.z, 1.2D);
        } else if (raider.getTarget() != null) {
            raider.getNavigation().stop();
        }
        intervalCooldown++;
    }
}
