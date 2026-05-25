package net.zerocontact.entity.ai.controller;

import net.minecraft.network.chat.Component;
import net.zerocontact.EnvHelper;
import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.ShareContext;
import net.zerocontact.entity.ai.controller.phase.AttackPhaseContext;
import net.zerocontact.entity.ai.controller.phase.ChasePhaseContext;
import net.zerocontact.entity.ai.controller.phase.EscapePhaseContext;
import net.zerocontact.entity.ai.controller.phase.IdlePhaseContext;

import java.util.EnumMap;
import java.util.Map;

public class GlobalStateController {
    public final VoiceManager voiceManager;

    public enum Phase {
        IDLE(9999), ATTACK(200), ESCAPE(40), CHASE(100);
        public final int timeOut;
        public final int minDuration = 15;

        Phase(int timeOut) {
            this.timeOut = timeOut;
        }
    }

    public static class VoiceManager {
        private final Map<Voice, Integer> voicesIntegerMap = new EnumMap<>(Voice.class);

        public void tick() {
            voicesIntegerMap.replaceAll(((__, cooldown) -> Math.max(0, cooldown - 1)));
        }

        public boolean canPlay(Voice voice) {
            return voicesIntegerMap.getOrDefault(voice, 0) == 0;
        }

        public boolean tryUse(Voice voice, int cooldown) {
            if (!canPlay(voice)) {
                return false;
            }
            voicesIntegerMap.put(voice, cooldown);
            return true;
        }

        public void reset(Voice voice) {
            voicesIntegerMap.remove(voice);
        }

        public void resetAll() {
            voicesIntegerMap.clear();
        }
    }

    public enum Voice {
        CONTACT,
        RELOAD,
        HURT
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
        this.voiceManager = new VoiceManager();
        this.armedRaider = entity;
        this.currentContext = new IdlePhaseContext(entity);
    }

    public void tick() {
        if (armedRaider.level().isClientSide) return;
        currentContext.tick();
        voiceManager.tick();
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
        if(EnvHelper.DEBUG){
            armedRaider.setCustomName(
                    Component.literal(
                            phase.name()
                                    + "-"
                                    + "IfHurt:"
                                    + armedRaider.stateController.shareContext.isHurt
                                    + "Target:"
                                    + (armedRaider.getTarget() == null ? "null" : armedRaider.getTarget().position())));
            armedRaider.setCustomNameVisible(true);
        }
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
