package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.entity.ai.controller.GlobalStateController;

public class TailGoal extends Goal {
    private Vec3 cacheTargetPos;
    private final ArmedRaider raider;
    private int intervalCooldown;
    public TailGoal(ArmedRaider mob) {
        this.raider = mob;
    }

    @Override
    public boolean canUse() {
        return raider.stateController.getPhase() == GlobalStateController.Phase.CHASE;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        chase();
    }

    public  static <T extends ArmedRaider>boolean canChaseTarget(T raider,TailGoal goal) {
        if (raider.getTarget() != null) {
            goal.cacheTargetPos = raider.getTarget().position();
            return false;
        }
        return goal.cacheTargetPos != null && raider.getTarget()==null;
    }
    private void chase(){
        if(intervalCooldown==40 && raider.getNavigation().isDone()){
            intervalCooldown=0;
            raider.getNavigation().moveTo(cacheTargetPos.x, cacheTargetPos.y, cacheTargetPos.z, 1.2D);
        }
        else{
            intervalCooldown++;
        }
    }
}
