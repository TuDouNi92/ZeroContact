package net.zerocontact.caliber.damage;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;
import org.jetbrains.annotations.Nullable;

/** The original TaCZ collision, available while onHitEntity is processing it. */
public interface BulletHitAccessor {
    @Nullable EntityKineticBullet.EntityResult zc$getCurrentHit();

    @Nullable HitPart zc$getResolvedHitPart(LivingEntity target);

    void zc$setResolvedHitPart(LivingEntity target, HitPart part);
}
