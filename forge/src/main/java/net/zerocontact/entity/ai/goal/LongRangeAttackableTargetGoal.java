package net.zerocontact.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class LongRangeAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final double searchY;

    public LongRangeAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustReach, double searchY) {
        super(mob, targetType, mustSee, mustReach);
        this.searchY = searchY;
    }

    @Override
    protected @NotNull AABB getTargetSearchArea(double targetDistance) {
        return this.mob.getBoundingBox().inflate(targetDistance, searchY, targetDistance);
    }
}
