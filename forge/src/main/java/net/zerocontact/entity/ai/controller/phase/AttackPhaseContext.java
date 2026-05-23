package net.zerocontact.entity.ai.controller.phase;

import net.minecraft.world.entity.PathfinderMob;
import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;

public class AttackPhaseContext implements IPhaseContext {
    private final ArmedRaider armedRaider;
    private int ticks = 0;

    public AttackPhaseContext(ArmedRaider armedRaider) {
        this.armedRaider = armedRaider;
    }

    @Override
    public void tick() {
        ticks++;
        cacheTargetIfNoVision();
    }

    private void cacheTargetIfNoVision() {

        if(!PerformGunAttackGoal.isInVisionToShoot(armedRaider)){
            armedRaider.stateController.getShareContext().cacheTarget = (PathfinderMob) armedRaider.getTarget();
            armedRaider.stateController.getShareContext().signalPhases.add(GlobalStateController.SignalPhase.WANTS_CHASE);
        }
    }

    @Override
    public int getTick() {
        return ticks;
    }

    @Override
    public boolean shouldTransition() {
        return !PerformGunAttackGoal.isInVisionToShoot(armedRaider);
    }

    @Override
    public GlobalStateController.Phase getNextPhase() {
        return GlobalStateController.Phase.IDLE;
    }

    @Override
    public boolean isTimedOut() {
        return ticks >= GlobalStateController.Phase.ATTACK.timeOut;
    }

    @Override
    public void onExit() {
        if(armedRaider.getTarget()!=null && !armedRaider.getTarget().isAlive()) armedRaider.setTarget(null);
    }
}
