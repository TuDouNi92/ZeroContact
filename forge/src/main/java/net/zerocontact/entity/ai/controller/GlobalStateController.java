package net.zerocontact.entity.ai.controller;

import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.ShareContext;

public class GlobalStateController {
    public enum Phase {
        IDLE, ATTACK, ESCAPE, CHASE
    }

    private Phase phase = Phase.IDLE;
    private int phaseTicks = 0;
    private final ArmedRaider entity;
    private final ShareContext shareContext = new ShareContext();
    public <T extends ArmedRaider> GlobalStateController(T entity) {
        this.entity = entity;
    }

    public void tick() {
        phaseTicks++;
                if (shareContext.canChaseTarget) {
                    updatePhase(Phase.CHASE);
                } else if (shareContext.canSeeTarget || shareContext.cacheTarget!=null) {
                    updatePhase(Phase.ATTACK);
                } else {
                    updatePhase(Phase.IDLE);
                }
        if (phaseTicks >= 40) {
            updatePhase(Phase.IDLE);
        }
    }

    public void updatePhase(Phase newPhase) {
        this.phase = newPhase;
        this.phaseTicks = 0;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public int getPhaseTicks() {
        return this.phaseTicks;
    }

    public ShareContext getShareContext() {
        return shareContext;
    }

    public void onHurt() {
        updatePhase(Phase.ESCAPE);
    }
}
