package net.zerocontact.entity.ai.goal;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.entity.ArmedRaider;

import java.util.List;
import java.util.function.Predicate;

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
        return mob.isHurt;
    }

    @Override
    public void tick() {
        runAway();
    }

    private void runAway() {
        if (!mob.getNavigation().isDone()) return;
        if (mob.getTarget() == null) {
            List<EntityKineticBullet> bullets = mob.level().getEntitiesOfClass(EntityKineticBullet.class, mob.getBoundingBox().inflate(4));
            if (bullets.isEmpty()) {
                int targetX = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
                int targetZ = (random.nextBoolean() ? 1 : -1) * random.nextInt(distance);
                Vec3 targetPos = mob.position().add(targetX, 0, targetZ);
                mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
            }
            else{
                EntityKineticBullet lastBullet =bullets.get(bullets.size()-1);
                Vec3 targetPos = LandRandomPos.getPosAway(mob, 12, 6, lastBullet.position());
                if (targetPos == null) return;
                mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
            }
        } else {
            Vec3 targetPos = LandRandomPos.getPosAway(mob, 12, 6, mob.getTarget().position());
            if (targetPos == null) return;
            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.5D);
        }
    }
}
