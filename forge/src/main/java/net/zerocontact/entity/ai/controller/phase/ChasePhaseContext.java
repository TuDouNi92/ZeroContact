package net.zerocontact.entity.ai.controller.phase;

import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.TailGoal;

public class ChasePhaseContext implements IPhaseContext {
    private final ArmedRaider armedRaider;
    private int ticks =0;

    public ChasePhaseContext(ArmedRaider armedRaider) {
        this.armedRaider = armedRaider;
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public boolean shouldTransition() {
        return !TailGoal.canChaseTarget(armedRaider);
    }

    @Override
    public GlobalStateController.Phase getNextPhase() {
        return GlobalStateController.Phase.IDLE;
    }

    @Override
    public boolean isTimedOut() {
        return ticks>=GlobalStateController.Phase.CHASE.timeOut;
    }

    @Override
    public void onExit() {
        armedRaider.stateController.getShareContext().cacheTarget=null;
    }
}
