package net.zerocontact.entity.ai.goal;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

public class MAvoidGoal extends Goal {
    private final ArmedRaider armedRaider;
    private final int distance;
    private final RandomSource random;

    public MAvoidGoal(ArmedRaider mob, int distance) {
        this.armedRaider = mob;
        this.distance = distance;
        this.random = mob.getRandom();
    }

    @Override
    public boolean canUse() {
        return armedRaider.stateController.getPhase() == GlobalStateController.Phase.ESCAPE;
    }

    @Override
    public void tick() {
        runAway();
    }

    private void runAway() {
        if (armedRaider.getTarget() == null) {
            int targetX = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
            int targetZ = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
            Vec3 targetPos = armedRaider.position().add(targetX, 0, targetZ);
            armedRaider.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
        } else if(!armedRaider.hasLineOfSight(armedRaider.getTarget())) {
            Vec3 targetPos = LandRandomPos.getPosAway(armedRaider, 12, 6, armedRaider.getTarget().position());
            if (targetPos == null) return;
            armedRaider.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
        }else{
            armedRaider.getLookControl().setLookAt(armedRaider.getTarget().position());
            armedRaider.stateController.getShareContext().signalPhases.add(GlobalStateController.SignalPhase.WANTS_ATTACK);
        }

    }
}
