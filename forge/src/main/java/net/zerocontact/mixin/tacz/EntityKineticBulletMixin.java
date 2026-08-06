package net.zerocontact.mixin.tacz;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.caliber.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = EntityKineticBullet.class)
public class EntityKineticBulletMixin extends Projectile {
    protected EntityKineticBulletMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;setPos(DDD)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    protected void onBulletTickHook(CallbackInfo ci, Vec3 movement, double x, double y, double z, double distance, double nextPosX, double nextPosY, double nextPosZ) {
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
