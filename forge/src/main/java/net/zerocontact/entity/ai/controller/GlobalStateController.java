package net.zerocontact.entity.ai.controller;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.forge.EnvHelper;
import net.zerocontact.api.IPhaseContext;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.ShareContext;
import net.zerocontact.entity.ai.controller.phase.AttackPhaseContext;
import net.zerocontact.entity.ai.controller.phase.ChasePhaseContext;
import net.zerocontact.entity.ai.controller.phase.EscapePhaseContext;
import net.zerocontact.entity.ai.controller.phase.IdlePhaseContext;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

        private boolean tryUse(Voice voice) {
            if (!canPlay(voice)) {
                return false;
            }
            voicesIntegerMap.put(voice, voice.coolDown);
            return true;
        }

        public void playSound(Voice voice, LivingEntity entity, Supplier<Boolean> supplier) {
            if (tryUse(voice) && supplier.get()) {
                voice.playSound(entity);
                voice.entityConsumer.accept(entity);
            }
        }
    }

    public enum Voice {
        CONTACT(200, ModSoundEventsReg.RAIDER_CONTACT_1, ModSoundEventsReg.RAIDER_CONTACT_2),
        RELOAD(100,
                entity -> {
                    IGun gun = IGun.getIGunOrNull(entity.getMainHandItem());
                    Optional.ofNullable(gun).ifPresent(iGun -> SoundManager.sendSoundToNearby(
                            entity,
                            6,
                            iGun.getGunId(entity.getMainHandItem()), iGun.getGunDisplayId(entity.getMainHandItem()),
                            SoundManager.RELOAD_EMPTY_SOUND, 1, 1));
                },
                ModSoundEventsReg.RAIDER_RELOAD_1, ModSoundEventsReg.RAIDER_RELOAD_2),
        HURT(40, ModSoundEventsReg.RAIDER_HIT_1, ModSoundEventsReg.RAIDER_HIT_2, ModSoundEventsReg.RAIDER_HIT_3);

        public final int coolDown;
        private final Set<SoundEvent> soundEventSet;
        private final Consumer<LivingEntity> entityConsumer;

        Voice(int coolDown, SoundEvent... soundEvent) {
            this.coolDown = coolDown;
            this.entityConsumer = e -> {
            };
            this.soundEventSet = new HashSet<>(Arrays.stream(soundEvent).toList());
        }

        Voice(int coolDown, Consumer<LivingEntity> entityConsumer, SoundEvent... soundEvent) {
            this.coolDown = coolDown;
            this.entityConsumer = entityConsumer;
            this.soundEventSet = new HashSet<>(Arrays.stream(soundEvent).toList());
        }

        void playSound(LivingEntity entity) {
            SoundEvent pickedSound = ModSoundEventsReg.randomSound(soundEventSet);
            entity.playSound(pickedSound);
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
        if (EnvHelper.DEBUG) {
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
