package net.zerocontact.entity.ai.controller;

import net.minecraft.world.entity.ai.goal.Goal;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;
import net.zerocontact.entity.ai.goal.TailGoal;

import java.util.ArrayList;

public class GlobalStateController {
    public enum Phase {
        IDLE, ATTACK, ESCAPE, CHASE
    }

    private Phase phase = Phase.IDLE;
    private int phaseTicks = 0;
    private final ArmedRaider entity;
    private final ArrayList<Goal> goals = new ArrayList<>();

    public <T extends ArmedRaider> GlobalStateController(T entity) {
        this.entity = entity;
    }

    public <T extends Goal> void appendGoals(T goal) {
        goals.add(goal);
    }

    public void tick() {
        phaseTicks++;

        for (Goal goal : goals) {
            if (goal instanceof TailGoal tailGoal) {
                if (tailGoal.canChaseTarget(entity)) {
                    updatePhase(Phase.CHASE);
                }
            }
        }

        if (PerformGunAttackGoal.canSee(entity)) {
            updatePhase(Phase.ATTACK);
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

    public void onHurt() {
        updatePhase(Phase.ESCAPE);
    }
}
