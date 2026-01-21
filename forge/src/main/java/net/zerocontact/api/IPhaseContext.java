package net.zerocontact.api;

import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

public interface IPhaseContext{
    void tick();
    int getTick();
    boolean shouldTransition();
    GlobalStateController.Phase getNextPhase();
    boolean isTimedOut();
    default void onEnter(){};
    default void onExit(){};
}
