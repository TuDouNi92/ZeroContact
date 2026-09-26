package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.compat.ReloadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;

@Mixin(value = GunAnimationStateContext.class, remap = false)
public abstract class GunAnimationStateContextMixin {
    @Shadow
    private IGun iGun;
    @Shadow
    private ItemStack currentGunItem;

    @Shadow
    protected abstract <T> Optional<T> processRemoteGunOperator(Function<IGunOperator, T> processor);


    @Shadow
    protected abstract <T> Optional<T> processCameraEntity(Function<Entity, T> processor);

    @Inject(method = "hasAmmoToConsume", at = @At("HEAD"), cancellable = true)
    private void hasAmmoToConsume(CallbackInfoReturnable<Boolean> cir) {
        if (!(Boolean) this.processRemoteGunOperator(IGunOperator::needCheckAmmo).orElse(true)) {
            cir.setReturnValue(true);
        } else if (this.iGun.useDummyAmmo(this.currentGunItem)) {
            cir.setReturnValue(this.iGun.getDummyAmmoAmount(this.currentGunItem) > 0);
        } else {
            cir.setReturnValue(this.processCameraEntity((entity -> {
                        if (entity instanceof LivingEntity shooter) {
                            ReloadManager.ReloadInventory inv = ReloadManager.resolveReloadInv(shooter);
                            for (int i = 0; i < inv.rawHandler().getSlots(); i++) {
                                ItemStack checkAmmoStack = inv.rawHandler().getStackInSlot(i);
                                if (checkAmmoStack.getItem() instanceof IAmmo iAmmo) {
                                    if (iAmmo.isAmmoOfGun(this.currentGunItem, checkAmmoStack)) {
                                        return true;
                                    }
                                }
                                if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox) {
                                    if (iAmmoBox.isAmmoBoxOfGun(this.currentGunItem, checkAmmoStack)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    })).orElse(false)
            );
        }
    }
}
