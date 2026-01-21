package net.zerocontact.entity.ai.controller.phase;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.Optional;

public class EscapePhaseContext implements IPhaseContext {
    private final ArmedRaider armedRaider;
    private int ticks = 0;

    public EscapePhaseContext(ArmedRaider armedRaider) {
        this.armedRaider = armedRaider;
    }

    @Override
    public void tick() {
        ticks++;
        int disposableTick =15;
        if (armedRaider.random.nextFloat() <= 0.65F && ticks ==disposableTick){
            Optional.ofNullable(armedRaider.getTarget()).ifPresent(target->{
                armedRaider.lookAt(EntityAnchorArgument.Anchor.EYES,target.position());
            });
            onExit();
        }
    }

    @Override
    public int getTick() {
        return ticks;
    }

    @Override
    public boolean shouldTransition() {
        return !armedRaider.stateController.getShareContext().isHurt;
    }

    @Override
    public GlobalStateController.Phase getNextPhase() {
        return GlobalStateController.Phase.IDLE;
    }

    @Override
    public boolean isTimedOut() {
        return ticks >= GlobalStateController.Phase.ESCAPE.timeOut;
    }

    @Override
    public void onExit() {
        armedRaider.stateController.getShareContext().isHurt = false;
    }
}
