package net.zerocontact.entity.ai.controller;

import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.ShareContext;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;
import net.zerocontact.entity.ai.goal.TailGoal;

import java.util.List;
import java.util.function.Predicate;

public class GlobalStateController {
    public enum Phase {
        IDLE(9999), ATTACK(100), ESCAPE(40), CHASE(100);
        private final int timeOut;

        Phase(int timeOut) {
            this.timeOut = timeOut;
        }

        public boolean isTimedOut(int ticks) {
            return ticks >= timeOut;
        }
    }

    private Phase phase = Phase.IDLE;
    private int phaseTicks = 0;
    private final ArmedRaider entity;
    private ShareContext shareContext = new ShareContext();
    private final List<PhaseRule> phaseRuleList = List.of(
            new PhaseRule(Phase.ESCAPE, ctx -> ctx.isHurt),
            new PhaseRule(Phase.CHASE, ctx -> ctx.canChaseTarget),
            new PhaseRule(Phase.ATTACK, ctx -> ctx.canSeeTarget),
            new PhaseRule(Phase.IDLE, __ -> phaseTicks >= 100)
    );

    record PhaseRule(Phase phase, Predicate<ShareContext> condition) {
    }

    public <T extends ArmedRaider> GlobalStateController(T entity) {
        this.entity = entity;
    }

    public void tick() {
        phaseTicks++;
        updateContext();
        for (PhaseRule rule : phaseRuleList) {
            if (rule.condition.test(shareContext)) {
                updatePhase(rule.phase, false);
                break;
            }
        }
    }

    public void updatePhase(Phase newPhase, boolean force) {
        if (!force) {
            if (newPhase == phase) return;
        }
        this.phaseTicks = 0;
        this.phase = newPhase;
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
        shareContext.isHurt = true;
    }

    void updateContext() {
        if (phase.isTimedOut(phaseTicks)) {
            shareContext = new ShareContext();
            updatePhase(Phase.IDLE,true);
        }
        if (shareContext.canChaseTarget != TailGoal.canChaseTarget(entity)) {
            shareContext.canChaseTarget = TailGoal.canChaseTarget(entity);
        }
        if (shareContext.canSeeTarget != PerformGunAttackGoal.canSee(entity)) {
            shareContext.canChaseTarget = PerformGunAttackGoal.canSee(entity);
        }
    }
}
