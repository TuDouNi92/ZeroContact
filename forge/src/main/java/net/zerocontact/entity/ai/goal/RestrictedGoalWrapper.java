package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class RestrictedGoalWrapper<T extends Goal, E extends ArmedRaider> extends Goal {
    private final E entity;
    private final T wrapped;
    private final Set<GlobalStateController.Phase> allowedPhases;

    private RestrictedGoalWrapper(E entity, T wrapped, GlobalStateController.Phase... allowedPhases) {
        Set<GlobalStateController.Phase> phaseSet;
        this.entity = entity;
        this.wrapped = wrapped;
        if (allowedPhases.length == 0) {
            phaseSet = EnumSet.of(GlobalStateController.Phase.IDLE);
        } else {
            phaseSet = EnumSet.copyOf(Arrays.stream(allowedPhases).toList());
        }
        this.allowedPhases = phaseSet;
    }

    public static <T extends Goal, E extends ArmedRaider> RestrictedGoalWrapper<T, E> create(E entity, T wrapped, GlobalStateController.Phase... allowedPhases) {
        return new RestrictedGoalWrapper<>(entity, wrapped, allowedPhases);
    }

    @Override
    public boolean canUse() {
        return allowedPhases.contains(entity.stateController.getPhase()) && wrapped.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return allowedPhases.contains(entity.stateController.getPhase()) && wrapped.canContinueToUse();
    }

    @Override
    public void start() {
        wrapped.start();
    }

    @Override
    public void stop() {
        wrapped.stop();
    }

    @Override
    public void tick() {
        wrapped.tick();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return wrapped.requiresUpdateEveryTick();
    }
}
