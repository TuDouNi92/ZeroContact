package net.zerocontact.entity.ai.goal;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;
import org.jetbrains.annotations.Nullable;

public class MAvoidGoal extends Goal {
    private final ArmedRaider mob;
    private final int distance;
    @Nullable
    private final RandomSource random;

    public MAvoidGoal(ArmedRaider mob, int distance) {
        this.mob = mob;
        this.distance = distance;
        this.random = mob.getRandom();
    }

    @Override
    public boolean canUse() {
        return mob.isHurt;
    }

    @Override
    public void tick() {
        if (random == null) return;
        int targetX = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
        int targetZ = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
        Vec3 targetPos = mob.position().add(targetX, 0, targetZ);
        if (!mob.getNavigation().isDone()) return;
        mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
    }
}
