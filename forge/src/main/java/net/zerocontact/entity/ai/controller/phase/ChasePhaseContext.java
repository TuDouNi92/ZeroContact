package net.zerocontact.entity.ai.controller.phase;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;
import org.jetbrains.annotations.Nullable;

public class ChasePhaseContext implements IPhaseContext {
    private final ArmedRaider raider;
    private int ticks = 0;
    private @Nullable LivingEntity cacheTarget;

    public ChasePhaseContext(ArmedRaider raider) {
        this.raider = raider;
    }

    @Override
    public void tick() {
        ticks++;
        tickChase();
    }

    private void tickChase() {
        this.cacheTarget = raider.stateController.getShareContext().cacheTarget;
        if (PerformGunAttackGoal.isInVisionToShoot(raider)) {
            raider.getNavigation().stop();
            raider.stateController.getShareContext().signalPhases.add(GlobalStateController.SignalPhase.WANTS_ATTACK);
            return;
        }
        if (cacheTarget != null) {
            Vec3 searchPos = LandRandomPos.getPosTowards(this.raider, 12, 6, cacheTarget.position());
            if (searchPos == null) return;
            if (!raider.getNavigation().isDone()) return;
            raider.getNavigation().moveTo(searchPos.x, searchPos.y, searchPos.z, 1D);
            raider.setTarget(cacheTarget);
        } else if (raider.getTarget() != null) {
            Vec3 searchPos = LandRandomPos.getPosTowards(this.raider, 12, 6, raider.getTarget().position());
            if (searchPos == null) return;
            if (!raider.getNavigation().isDone()) return;
            raider.getNavigation().moveTo(searchPos.x, searchPos.y, searchPos.z, 1D);
        }

    }

    @Override
    public int getTick() {
        return ticks;
    }

    @Override
    public boolean shouldTransition() {
        return (cacheTarget == null || !cacheTarget.isAlive()) && (raider.getTarget() == null || !raider.getTarget().isAlive());
    }

    @Override
    public GlobalStateController.Phase getNextPhase() {
        return GlobalStateController.Phase.IDLE;
    }

    @Override
    public boolean isTimedOut() {
        return ticks >= GlobalStateController.Phase.CHASE.timeOut;
    }

    @Override
    public void onExit() {
        raider.stateController.getShareContext().cacheTarget = null;
    }
}
