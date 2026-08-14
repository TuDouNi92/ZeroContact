package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineReloadSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MagazineReloadSource.class)
public class MagazineReloadSourceMixin {
    @Inject(
            method = "createCreativeReloadMagazine",
            at = @At("RETURN"),
            remap = false,
            cancellable = true)
    private static void zeroContact$creativePatch(IItemHandler inventory, ItemStack gun, int selectedSlot, CallbackInfoReturnable<ItemStack> cir) {
        if (selectedSlot >= 0) {
            ItemStack selectedItem = inventory.getStackInSlot(selectedSlot);
            AmmoInjector.AmmoContext ammoContext = AmmoInjector.read(selectedItem);
            gun.getCapability(CapabilityRegistries.CARTRIDGE).ifPresent(cap -> {
                        if (ammoContext.isEmpty()) {
                            cap.setDefaultAmmoVariantInGun(gun);
                            return;
                        }
                        cap.setAmmoVariantInGun(gun, ammoContext.caliber().variant());
                        cap.setClientSelectedAmmoVariant(gun, ammoContext.caliber().variant());
                    }
            );
        } else {
            ItemStack magWithExtInfo = MagazinesCompatHandler.get().getCompat().map(compat -> compat.getCompatibleMag(gun)).orElse(ItemStack.EMPTY);
            AmmoInjector.AmmoContext gunContext = AmmoInjector.read(gun);
            if (gunContext.isEmpty()) return;
            if (!gunContext.caliber().variant().equals("tacz:ammo")) {
                AmmoInjector.write(gunContext, magWithExtInfo);
            }
            if (magWithExtInfo.isEmpty()) return;
            cir.setReturnValue(magWithExtInfo);
        }
    }
}
