package net.zerocontact.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.zerocontact.item.rigs.BaseRigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(GunHudOverlay.class)
public class GunHudOverlayMixin {
    @Shadow(remap = false)
    private static int cacheInventoryAmmoCount;

    @Inject(method = "handleInventoryAmmo", at = @At("RETURN"), remap = false)
    private static void zeroContact$handleInventoryAmmo(ItemStack stack, Inventory inventory, CallbackInfo ci) {
        CuriosApi.getCuriosInventory(inventory.player).ifPresent(curioHandler -> {
            curioHandler.getStacksHandler("rigs").ifPresent(stacksHandler -> {
                ItemStack rigsStack = stacksHandler.getStacks().getStackInSlot(0);
                if (rigsStack.getItem() instanceof BaseRigs) {
                    rigsStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).map(itemHandler -> {
                        for (int i = 0; i < itemHandler.getSlots(); ++i) {
                            ItemStack slotStack = itemHandler.getStackInSlot(i);
                            if (slotStack.getItem() instanceof IAmmo iAmmo) {
                                if (iAmmo.isAmmoOfGun(stack, slotStack)) {
                                    cacheInventoryAmmoCount += slotStack.getCount();
                                }
                            }
                        }
                        return cacheInventoryAmmoCount;
                    });
                }
            });
        });
    }
}
