package net.zerocontact.entity.ai.controller;

import net.minecraft.network.chat.Component;
import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.ShareContext;
import net.zerocontact.entity.ai.controller.phase.AttackPhaseContext;
import net.zerocontact.entity.ai.controller.phase.ChasePhaseContext;
import net.zerocontact.entity.ai.controller.phase.EscapePhaseContext;
import net.zerocontact.entity.ai.controller.phase.IdlePhaseContext;

public class GlobalStateController {
    public enum Phase {
        IDLE(9999), ATTACK(200), ESCAPE(40), CHASE(100);
        public final int timeOut;
        public final int minDuration = 15;

        Phase(int timeOut) {
            this.timeOut = timeOut;
        }
    }

    public enum SignalPhase {
        WANTS_CHASE,
        WANTS_ATTACK
    }

    private Phase phase = Phase.IDLE;
    private final ArmedRaider armedRaider;
    private final ShareContext shareContext = new ShareContext();
    private IPhaseContext currentContext;

    public <T extends ArmedRaider> GlobalStateController(T entity) {
        this.armedRaider = entity;
        this.currentContext = new IdlePhaseContext(entity);
    }

    public void tick() {
        if (armedRaider.level().isClientSide) return;
        currentContext.tick();
        if (!checkSignal()) {
            updatePhase();
        }
    }

    boolean checkSignal() {
        if (currentContext.getTick() < phase.minDuration) {
            ignoreSignal();
            return false;
        }
        if (shareContext.signalPhases.contains(SignalPhase.WANTS_ATTACK)) {
            updateContext(Phase.ATTACK);
            return true;
        } else if (shareContext.signalPhases.contains(SignalPhase.WANTS_CHASE)) {
            updateContext(Phase.CHASE);
            return true;
        }
        ignoreSignal();
        return false;
    }

    void ignoreSignal() {
        shareContext.signalPhases.clear();
    }

    void updatePhase() {
        if (this.currentContext.isTimedOut() || this.currentContext.shouldTransition()) {
            Phase nextPhase = currentContext.getNextPhase();
            if (nextPhase == phase) return;
            updateContext(nextPhase);
        }
    }

    private void updateContext(Phase newPhase) {

        this.phase = newPhase;
        this.currentContext.onExit();
        this.currentContext = switch (newPhase) {
            case CHASE -> new ChasePhaseContext(armedRaider);
            case IDLE -> new IdlePhaseContext(armedRaider);
            case ATTACK -> new AttackPhaseContext(armedRaider);
            case ESCAPE -> new EscapePhaseContext(armedRaider);
        };
        this.currentContext.onEnter();
//        armedRaider.setCustomName(
//                Component.literal(
//                        phase.name()
//                                + "-"
//                                + "IfHurt:"
//                                + armedRaider.stateController.shareContext.isHurt
//                                + "Target:"
//                                + (armedRaider.getTarget() == null ? "null" : armedRaider.getTarget().position())));
    }

    public Phase getPhase() {
        return this.phase;
    }

    public ShareContext getShareContext() {
        return shareContext;
    }

    public void onHurt() {
        shareContext.isHurt = true;
    }


}
