package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.capability.GunMagazineCapability;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunMagazineCapability.class, remap = false)
public class GunMagazineCapabilityMixin {
    @Shadow @Final private ItemStack gunStack;

    @Inject(method = "setStoredMagazine", at = @At("HEAD"))
    private void zeroContact$store(ItemStack magazine, CallbackInfo ci) {
        MagazinesCompatHandler.get().getCompat().ifPresent(compat -> compat.beforeStoreMagazine(gunStack, magazine));
    }
}
