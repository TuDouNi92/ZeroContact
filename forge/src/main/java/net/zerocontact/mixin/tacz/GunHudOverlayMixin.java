package net.zerocontact.mixin.tacz;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.caliber.compat.ReloadManager;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.compat.MagazinesCompatHandler;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(value = GunHudOverlay.class, priority = 999)
public class GunHudOverlayMixin {
    @Shadow(remap = false)
    private static int cacheInventoryAmmoCount;

    @Inject(method = "handleInventoryAmmo", at = @At("HEAD"), remap = false, cancellable = true)
    private static void zeroContact$handleInventoryAmmo(ItemStack stack, Inventory inventory, CallbackInfo ci) {
        Item checkItem = stack.getItem();
        if (checkItem instanceof IGun iGun) {
            ResourceLocation gunId = iGun.getGunId(stack);
            CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
            if (gunIndex != null) {
                ReloadManager.ReloadInventory reloadInventory = ReloadManager.resolveReloadInv(inventory.player);
                IItemHandler itemHandler = reloadInventory.rawHandler();
                Supplier<Integer> countAmmo = () -> {
                    int count = 0;
                    for (int i = 0; i < itemHandler.getSlots(); ++i) {
                        ItemStack inventoryAmmo = itemHandler.getStackInSlot(i);
                        if (inventoryAmmo.getItem() instanceof IAmmo iAmmo) {
                            if (iAmmo.isAmmoOfGun(stack, inventoryAmmo)) {
                                count += inventoryAmmo.getCount();
                            }
                        }
                        if (inventoryAmmo.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, inventoryAmmo)) {
                            if (iAmmoBox.isAllTypeCreative(inventoryAmmo) || iAmmoBox.isCreative(inventoryAmmo)) {
                                count = 9999;
                                return count;
                            }
                            count += iAmmoBox.getAmmoCount(inventoryAmmo);
                        }
                    }
                    return count;
                };
                MagazinesCompatHandler.get().getCompat().ifPresentOrElse(compat -> {
                            if (compat.isMagazineCompatibleWithGun(stack)) {
                                int total = 0;
                                for (int i = 0; i < itemHandler.getSlots(); ++i) {
                                    ItemStack slot = itemHandler.getStackInSlot(i);
                                    if (compat.instanceOfMagazine(slot.getItem())) {
                                        if (compat.isAmmoBoxOfGun(stack, slot)) {
                                            int ammoPerMag = compat.getAmmoCount(slot);
                                            if (ammoPerMag > 0) {
                                                total += ammoPerMag * slot.getCount();
                                            }
                                        }
                                    }
                                }
                                cacheInventoryAmmoCount = total;
                            } else {
                                cacheInventoryAmmoCount = countAmmo.get();
                            }
                        }
                        , () -> {
                            cacheInventoryAmmoCount = countAmmo.get();

                        }
                );
                ci.cancel();
            }
        }

    }

    @Inject(method = "render", remap = false, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER, remap = true), locals = LocalCapture.CAPTURE_FAILHARD)
    private void zeroContact$renderHud(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height, CallbackInfo ci, Minecraft mc, LocalPlayer player, ItemStack stack, IGun iGun, ResourceLocation gunId, GunData gunData, GunDisplayInstance display, boolean useInventoryAmmo, boolean useDummyAmmo, boolean overheatLocked, int ammoCount, int ammoCountColor, int inventoryAmmoCountColor, String currentAmmoCountText, String inventoryAmmoCountText, PoseStack poseStack, Font font) {
        if (player == null) return;
        if (!ModConfigs.CLIENT.ammoTypeOverLay().get()) return;
        ItemStack gunStack = IGun.mainHandHoldGun(player) ? player.getMainHandItem() : null;
        if (gunStack == null) return;
        gunStack.getCapability(CapabilityRegistries.CARTRIDGE).ifPresent(cap -> {
            String currentAmmoKey = cap.getAmmoVariantInGun(gunStack);
            ItemStack currentAmmo = cap.getDefaultStack(currentAmmoKey);
            Component ammoName = Component.literal("\uD83E\uDC35 ").append(Component.translatable(currentAmmo.getDescriptionId()));
            if (!(currentAmmo.getItem() instanceof GenerateAmmo))
                ammoName = Component.translatable("hud.zerocontact.ammo.default");
            float scale = 0.65f;
            int right = width - 117;
            int bottom = height - 56;
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1);
            graphics.drawString(
                    mc.font,
                    ammoName,
                    (int) Math.floor(right / scale),
                    (int) Math.floor(bottom / scale),
                    ammoCountColor,
                    false
            );

            poseStack.popPose();
        });
    }
}
