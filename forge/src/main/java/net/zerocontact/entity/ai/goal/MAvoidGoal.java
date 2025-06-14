package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class MAvoidGoal extends Goal {
    private final ArmedRaider mob;
    private final float distance;
    @Nullable
    private final Random random = new Random();

    public MAvoidGoal(ArmedRaider mob, float distance) {
        this.mob = mob;
        this.distance = distance;
    }

    @Override
    public boolean canUse() {
        return mob.isHurt;
    }

    @Override
    public void tick() {
        Vec3 targetPos =mob.position().add(0,0,-1);
        mob.getNavigation().moveTo(targetPos.x,targetPos.y,targetPos.z,1.0D);
    }

}
