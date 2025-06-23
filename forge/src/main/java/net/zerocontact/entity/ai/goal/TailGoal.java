package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;

public class TailGoal extends Goal {
    private Vec3 cacheTargetPos;
    private final ArmedRaider raider;
    private int intervalCooldown;

    public TailGoal(ArmedRaider mob) {
        this.raider = mob;
    }

    @Override
    public boolean canUse() {
        return canChaseTarget() && !raider.isHurt;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        chase();
    }

    private boolean canChaseTarget() {
        if (raider.getTarget() != null) {
            cacheTargetPos = raider.getTarget().position();
            return false;
        }
        return cacheTargetPos != null && raider.getTarget()==null;
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
