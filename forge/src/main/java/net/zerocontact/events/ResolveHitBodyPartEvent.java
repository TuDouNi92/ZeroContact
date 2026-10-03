package net.zerocontact.events;


import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Resolves the body part before armor selection and damage calculation. */
public class ResolveHitBodyPartEvent extends Event {

    private final LivingEntity hurtEntity;
    private final DamageSource source;
    private final @Nullable Vec3 hitPosition;
    protected @NotNull HitPart hitPart;

    protected ResolveHitBodyPartEvent(LivingEntity hurtEntity, DamageSource source,
                                      @Nullable Vec3 hitPosition, @NotNull HitPart part) {
        this.hurtEntity = Objects.requireNonNull(hurtEntity);
        this.source = Objects.requireNonNull(source);
        this.hitPosition = hitPosition;
        this.hitPart = Objects.requireNonNull(part);
    }

    public LivingEntity getHurtEntity() {
        return hurtEntity;
    }

    public DamageSource getSource() {
        return source;
    }

    public @Nullable Entity getBulletEntity() {
        return source.getDirectEntity();
    }

    public @Nullable Vec3 getHitPosition() {
        return hitPosition;
    }

    public @NotNull HitPart getHitPart() {
        return hitPart;
    }

    public static class Pre extends ResolveHitBodyPartEvent {
        @ApiStatus.Internal
        public Pre(LivingEntity hurtEntity, DamageSource source, @Nullable Vec3 hitPosition,
                   @NotNull HitPart part) {
            super(hurtEntity, source, hitPosition, part);
        }

        public void setHitPart(@NotNull HitPart hitPart) {
            this.hitPart = Objects.requireNonNull(hitPart);
        }

    }

    public static class Post extends ResolveHitBodyPartEvent {
        @ApiStatus.Internal
        public Post(LivingEntity hurtEntity, DamageSource source, @Nullable Vec3 hitPosition,
                    @NotNull HitPart part) {
            super(hurtEntity, source, hitPosition, part);
        }

    }
    public enum HitPartEnum {
        HEAD,
        TORSO,
        ARM,
        LEG,
        UNSET
    }

    /** damageFactor is applied once, after the part's damage has been calculated. */
    public record HitPart(
            HitPartEnum hitPart,
            float damageFactor
    ) {
        public HitPart {
            Objects.requireNonNull(hitPart);
            if (!Float.isFinite(damageFactor) || damageFactor < 0) {
                throw new IllegalArgumentException("Body part damage factor must be finite and non-negative");
            }
        }

        public boolean isLimb() {
            return hitPart == HitPartEnum.ARM || hitPart == HitPartEnum.LEG;
        }
    }
}
