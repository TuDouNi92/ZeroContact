package net.zerocontact.entity.ai.goal;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

public class MAvoidGoal extends Goal {
    private final ArmedRaider mob;
    private final int distance;
    private final RandomSource random;

    public MAvoidGoal(ArmedRaider mob, int distance) {
        this.mob = mob;
        this.distance = distance;
        this.random = mob.getRandom();
    }

    @Override
    public boolean canUse() {
        return mob.stateController.getPhase() == GlobalStateController.Phase.ESCAPE;
    }

    @Override
    public void tick() {
        runAway();
    }

    private void runAway() {
        if (!mob.getNavigation().isDone()) return;
        if (mob.getTarget() == null) {
            int targetX = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
            int targetZ = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
            Vec3 targetPos = mob.position().add(targetX, 0, targetZ);
            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
        } else {
            Vec3 targetPos = LandRandomPos.getPosAway(mob, 12, 6, mob.getTarget().position());
            if (targetPos == null) return;
            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
        }
    }
}
