package net.zerocontact.entity.ai.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
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

    protected boolean isTargetInSight(LivingEntity target) {
        return this.mob.hasLineOfSight(target) && PerformGunAttackGoal.isInVisionToShoot(this.mob, target);
    }

    @Override
    protected void findTarget() {
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), this::isTargetInSight), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.target = this.mob.level().getNearestPlayer(TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this::isTargetInSight), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }
    }
}
