package net.zerocontact.mixin.magazines;


import com.raiiiden.taczmagazines.client.ClientReloadKeyHandler;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.tacz.guns.client.input.ReloadKey;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.zerocontact.caliber.compat.ReloadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientReloadKeyHandler.class)
public class ClientReloadKeyHandlerMixin {

    @Shadow(remap = false)
    private static boolean keyDown;

    @Shadow(remap = false)
    private static boolean inSelectorMode;

    @Shadow(remap = false)
    public static void onReloadKeyReleased() {
        throw new AssertionError();
    }

    @Inject(method = "onClientTick", at = @At("RETURN"), remap = false)
    private static void zeroContact$releaseSelector(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        // Upstream returns from the selector branch before checking whether R was released.
        // Run after its cancellation/screen cleanup, including that early return.
        if (event.phase == TickEvent.Phase.END && keyDown && inSelectorMode
                && !ReloadKey.RELOAD_KEY.isDown()) {
            onReloadKeyReleased();
        }
    }

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
