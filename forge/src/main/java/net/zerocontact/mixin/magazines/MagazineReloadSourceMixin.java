package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineReloadSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.caliber.compat.MagazineReloadCheck;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MagazineReloadSource.class, remap = false)
public class MagazineReloadSourceMixin {
    @ModifyVariable(method = "hasUsableMagazine", at = @At("HEAD"), argsOnly = true)
    private static IItemHandler zeroContact$reloadCheckInventory(IItemHandler inventory) {
        return MagazineReloadCheck.resolveInventory(inventory);
    }

    @Inject(method = "createCreativeReloadMagazine", at = @At("RETURN"), cancellable = true)
    private static void zeroContact$creativeMagazine(IItemHandler inventory, ItemStack gun, int selectedSlot,
                                                     CallbackInfoReturnable<ItemStack> cir) {
        MagazinesCompatHandler.get().getCompat().ifPresent(compat ->
                cir.setReturnValue(compat.prepareCreativeMagazine(inventory, gun, selectedSlot, cir.getReturnValue())));
    }
}
