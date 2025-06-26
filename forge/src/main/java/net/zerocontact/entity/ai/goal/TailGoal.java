package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.function.Supplier;

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
        chase(()->canChaseTarget(raider));
    }

    private static <T extends ArmedRaider> boolean canChaseTarget(T raider) {
        if (raider.getTarget() != null) {
            raider.stateController.getShareContext().cacheTarget = raider.getTarget().position();
            raider.stateController.getShareContext().canChaseTarget =false;
            return false;
        }
        raider.stateController.getShareContext().canChaseTarget =true;
        return true;
    }

    private void chase(Supplier<Boolean> canChase) {
        if(!canChase.get())return;
        Vec3 cacheTargetPos = raider.stateController.getShareContext().cacheTarget;
        if (intervalCooldown == 40 && raider.getNavigation().isDone()) {
            intervalCooldown = 0;
            raider.getNavigation().moveTo(cacheTargetPos.x, cacheTargetPos.y, cacheTargetPos.z, 1.2D);
        } else {
            intervalCooldown++;
        }
    }
}
