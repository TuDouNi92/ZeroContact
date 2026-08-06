package net.zerocontact.caliber;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public enum TargetSelector {
    SHOOTER(),
    VICTIM(),
    NEARBY();

    @SuppressWarnings("resource")
    public Stream<LivingEntity> resolve(HookContext context, float radius) {
        return switch (this) {
            case SHOOTER -> Stream.ofNullable(context.shooter());
            case VICTIM -> Stream.ofNullable(context.victim());
            case NEARBY -> {
                Vec3 currentPos = context.positon();
                var ref = new Object() {
                    Vec3 prevPos = context.prevPos();
                };
                if (ref.prevPos == null) ref.prevPos = currentPos;
                AABB tracingAABB = new AABB(ref.prevPos, currentPos);
                yield context.level().getEntitiesOfClass(
                        LivingEntity.class,
                        tracingAABB.inflate(radius, 2, radius)
                ).stream().filter(lv -> {
                    boolean notSelf = !(lv.equals(context.shooter()));
                    AABB targetBox = lv.getBoundingBox().inflate(radius,2,radius);
                    boolean clip = targetBox.contains(ref.prevPos) || targetBox.clip(ref.prevPos, context.positon()).isPresent();
                    return notSelf && clip;
                });
            }
        };
    }
}
