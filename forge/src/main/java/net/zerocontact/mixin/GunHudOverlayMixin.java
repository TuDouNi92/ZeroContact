package net.zerocontact.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.zerocontact.events.AmmoInjector;
import net.zerocontact.item.rigs.BaseRigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
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
    @Inject(method = "render",remap = false,at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I",ordinal = 0,shift = At.Shift.AFTER),locals = LocalCapture.CAPTURE_FAILHARD)
    private void zeroContact$renderHud(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height, CallbackInfo ci, Minecraft mc, LocalPlayer player, ItemStack stack, IGun iGun, ResourceLocation gunId, GunData gunData, GunDisplayInstance display, boolean useInventoryAmmo, boolean useDummyAmmo, boolean overheatLocked, int ammoCount, int ammoCountColor, int inventoryAmmoCountColor, String currentAmmoCountText, String inventoryAmmoCountText, PoseStack poseStack, Font font){
        if(player==null)return;
        ItemStack gunStack = IGun.mainHandHoldGun(player)?player.getMainHandItem():null;
        if(gunStack==null)return;
        String[] currentAmmo = AmmoInjector.getAmmoVariantInGun(gunStack).split(":");
        if(currentAmmo.length<2)return;
        graphics.drawString(mc.font,currentAmmo[1],(float)(width - 75) / 1.5F, (float)(height - 72) / 1.5F,ammoCountColor,false);
    }
}
