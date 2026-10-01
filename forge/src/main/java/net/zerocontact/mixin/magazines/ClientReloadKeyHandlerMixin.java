package net.zerocontact.mixin.magazines;


import com.raiiiden.taczmagazines.client.ClientReloadKeyHandler;
import com.raiiiden.taczmagazines.item.MagazineItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.compat.ReloadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientReloadKeyHandler.class)
public class ClientReloadKeyHandlerMixin {

    @Redirect(
            method = "onReloadKeyReleased",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;", remap = true),
            remap = false)
    private static ItemStack redirectMagSlot(Inventory originalInventory, int slot) {
        if (slot >= 0) {
            ReloadManager.ReloadInventory inventory =
                    ReloadManager.resolveReloadInv(originalInventory.player);
            return inventory.rawHandler().getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Inject(method = "firstCompatibleMagazine", at = @At("HEAD"), remap = false, cancellable = true)
    private static void redirectNonSelectorMagSlot(ItemStack gun, Inventory inv, CallbackInfoReturnable<ItemStack> cir) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ReloadManager.ReloadInventory inventory = ReloadManager.resolveReloadInv(player);
        for (int i = 0; i < inventory.rawHandler().getSlots(); i++) {
            ItemStack stack = inventory.rawHandler().getStackInSlot(i);
            Item item = stack.getItem();
            if (item instanceof MagazineItem magazineItem) {
                if (magazineItem.isAmmoBoxOfGun(gun, stack)) {
                    cir.setReturnValue(stack);
                    return;
                }
            }

        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
