package net.zerocontact.caliber;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public enum TargetSelector {
    SHOOTER(),
    VICTIM(),
    NEARBY();

    public Stream<LivingEntity> resolve(HookContext context, float radius) {
        return switch (this) {
            case SHOOTER -> Stream.ofNullable(context.shooter());
            case VICTIM -> Stream.ofNullable(context.victim());
            case NEARBY -> context.level().getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(context.positon(), context.positon()).inflate(radius, context.positon().y + 2, radius)
            ).stream().filter(lv -> !(lv.equals(context.shooter())));
        };
    }
}
