package net.zerocontact.entity.ai.controller.phase;

import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;

import java.util.Optional;

public class AttackPhaseContext implements IPhaseContext {
    private final ArmedRaider armedRaider;
    private int ticks =0;
    public AttackPhaseContext(ArmedRaider armedRaider){
        this.armedRaider = armedRaider;
    }
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
        return !PerformGunAttackGoal.isInVisionToShoot(armedRaider);
    }

    @Override
    public GlobalStateController.Phase getNextPhase() {
        return GlobalStateController.Phase.IDLE;
    }

    @Override
    public boolean isTimedOut() {
        return ticks>=GlobalStateController.Phase.ATTACK.timeOut;
    }

    @Override
    public void onExit() {
        Optional.ofNullable(armedRaider.getTarget()).ifPresent(target->{
            if(!target.isAlive()){
                armedRaider.setTarget(null);
            }
        });
    }
}
