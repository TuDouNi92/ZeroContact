package net.zerocontact.entity.ai.controller.phase;

import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;

public class IdlePhaseContext implements IPhaseContext {
    private final ArmedRaider armedRaider;
    private int ticks = 0;

    public IdlePhaseContext(ArmedRaider armedRaider) {
        this.armedRaider = armedRaider;
    }

    ;

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public int getTick() {
        return ticks;
    }

    @Override
    public boolean shouldTransition() {
        return PerformGunAttackGoal.isInVisionToShoot(armedRaider)
                || armedRaider.stateController.getShareContext().isHurt;
    }

    @Override
    public GlobalStateController.Phase getNextPhase() {
        if (PerformGunAttackGoal.isInVisionToShoot(armedRaider)) return GlobalStateController.Phase.ATTACK;
        if (armedRaider.stateController.getShareContext().isHurt) return GlobalStateController.Phase.ESCAPE;
        return GlobalStateController.Phase.IDLE;
    }

    @Override
    public boolean isTimedOut() {
        return ticks >= GlobalStateController.Phase.IDLE.timeOut;
    }
}
