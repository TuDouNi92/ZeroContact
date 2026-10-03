package net.zerocontact.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.TacHitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.zerocontact.caliber.*;
import net.zerocontact.caliber.damage.BulletHitAccessor;
import net.zerocontact.caliber.damage.ZDamageTypes;
import net.zerocontact.caliber.damage.model.DamageResult;
import net.zerocontact.caliber.extension.HookDispatcher;
import net.zerocontact.caliber.extension.HookEventTrigger;
import net.zerocontact.caliber.extension.model.HookContext;
import net.zerocontact.events.HitProcessEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityKineticBullet.class)
public class EntityKineticBulletMixin extends Projectile implements BulletHitAccessor {
    @Unique
    private EntityKineticBullet.EntityResult zc$currentHit;

    @Unique
    private LivingEntity zc$resolvedTarget;

    @Unique
    private HitPart zc$resolvedHitPart;

    protected EntityKineticBulletMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    @Unique
    public EntityKineticBullet.EntityResult zc$getCurrentHit() {
        return zc$currentHit;
    }

    @Override
    @Unique
    public HitPart zc$getResolvedHitPart(LivingEntity target) {
        return zc$currentHit != null && zc$resolvedTarget == target ? zc$resolvedHitPart : null;
    }

    @Override
    @Unique
    public void zc$setResolvedHitPart(LivingEntity target, HitPart part) {
        if (zc$currentHit != null) {
            zc$resolvedTarget = target;
            zc$resolvedHitPart = part;
        }
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), remap = false)
    private void zc$captureHit(TacHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        zc$currentHit = new EntityKineticBullet.EntityResult(result.getEntity(), result.getLocation(), result.isHeadshot());
        zc$resolvedTarget = null;
        zc$resolvedHitPart = null;
    }

    @Inject(method = "onHitEntity", at = @At("RETURN"), remap = false)
    private void zc$clearHit(TacHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        zc$currentHit = null;
        zc$resolvedTarget = null;
        zc$resolvedHitPart = null;
    }

    @Inject(method = "tacAttackEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void zc$applyDamage(EntityKineticBullet.MaybeMultipartEntity parts, float damage,
                                Pair<DamageSource, DamageSource> sources, CallbackInfo ci) {
        DamageSource source = sources.getLeft();
        if (!source.is(ZDamageTypes.ZC_DAMAGE)) return;
        ZDamageTypes.ZDamageSource zDamageSource = ((ZDamageTypes.ZDamageSource) source);
        DamageResult result = zDamageSource.getLastResult();
        if (!zDamageSource.canSkip()) {
            // Replace only the split damage calls; onHitEntity still handles all later effects.
            parts.core().invulnerableTime = 0;
            parts.hitPart().hurt(source, damage);
            Entity attacker = source.getEntity();
            if (parts.core() instanceof LivingEntity hurt) {
                MinecraftForge.EVENT_BUS.post(new HitProcessEvent.Post(
                        attacker instanceof LivingEntity ? (LivingEntity) attacker : null,
                        hurt,
                        source.getDirectEntity(),
                        source,
                        result.armorContext(),
                        result.ammoContext(),
                        result.outcome(),
                        result.isHeadshot(),
                        damage
                ));
            }

            if (this.level() instanceof ServerLevel serverLevel
                    && attacker instanceof LivingEntity livingEntity
                    && parts.core() instanceof LivingEntity hurtEntity) {
                AmmoInjector.AmmoContext context = BulletBinder.getContext((EntityKineticBullet) (Object) this);
                if (context != null) {
                    HookDispatcher.fire(HookEventTrigger.HIT_ENTITY,
                            new HookContext(serverLevel, livingEntity, hurtEntity, hurtEntity.position(), context.caliber()));
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;setPos(DDD)V"))
    protected void onBulletTickHook(CallbackInfo ci, @Local(name = "nextPosX") double nextPosX, @Local(name = "nextPosY") double nextPosY, @Local(name = "nextPosZ") double nextPosZ) {
        if (this.level().isClientSide) return;
        AmmoInjector.AmmoContext context = BulletBinder.getContext((EntityKineticBullet) (Object) this);
        if (context == null) return;
        HookDispatcher.fire(
                HookEventTrigger.BULLET_TICKING,
                new HookContext(
                        (ServerLevel) this.level(),
                        (LivingEntity) this.getOwner(),
                        null,
                        this.position(),
                        new Vec3(nextPosX, nextPosY, nextPosZ),
                        context.caliber()
                )
        );
    }


    @Inject(method = "onHitBlock", at = @At("HEAD"), remap = false)
    protected void onHitBlockHook(BlockHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        if (this.level().isClientSide) return;
        if (!result.getType().equals(HitResult.Type.MISS)) {
            AmmoInjector.AmmoContext context = BulletBinder.getContext((EntityKineticBullet) (Object) this);
            if (context == null) return;
            HookContext hookContext = new HookContext(
                    (ServerLevel) this.level(),
                    (LivingEntity) this.getOwner(),
                    null,
                    new Vec3(result.getBlockPos().getCenter().toVector3f()),
                    context.caliber()
            );
            HookDispatcher.fire(
                    HookEventTrigger.HIT_BLOCK,
                    hookContext
            );
            HookDispatcher.fire(
                    HookEventTrigger.HIT_BLOCK_TICKING,
                    hookContext
            );
        }
    }

    @Override
    protected void defineSynchedData() {
    }
}
