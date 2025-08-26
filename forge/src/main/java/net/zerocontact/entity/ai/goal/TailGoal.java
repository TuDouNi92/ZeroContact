package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.Optional;

public class TailGoal extends Goal {
    private final ArmedRaider raider;

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
            raider.stateController.getShareContext().cacheTarget = raider.getTarget();
            return false;
        }
        return raider.stateController.getShareContext().cacheTarget != null
                && raider.stateController.getShareContext().cacheTarget.isAlive();
    }

    private void chase() {
        LivingEntity cacheTarget = raider.stateController.getShareContext().cacheTarget;
        Vec3 cacheTargetPos = cacheTarget.position();
        Optional.ofNullable(raider.getTarget()).ifPresentOrElse(target -> {
            if (!raider.hasLineOfSight(target)) {
                raider.getNavigation().moveTo(cacheTargetPos.x, cacheTargetPos.y, cacheTargetPos.z, 1.2D);
            } else {
                raider.getNavigation().stop();
                raider.stateController.getShareContext().signalPhases.add(GlobalStateController.SignalPhase.WANTS_ATTACK);
            }
        }, () -> {
            raider.setTarget(cacheTarget);
        });
    }
}
