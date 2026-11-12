package net.zerocontact.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.capability.CapabilityRegistry;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = GunHudOverlay.class, priority = 999)
public class GunHudOverlayMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 0, shift = At.Shift.AFTER))
    private void zeroContact$renderHud(
            GuiGraphics graphics, DeltaTracker delta,
            CallbackInfo ci,
            @Local(name = "stack") ItemStack stack, @Local(name = "ammoCountColor") int ammoCountColor,
            @Local(name = "width") int width, @Local(name = "height") int height
    ) {
        if (!ModConfigs.CLIENT.ammoTypeOverLay().get()) return;
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = graphics.pose();
        Optional.ofNullable(stack.getCapability(CapabilityRegistry.CARTRIDGE)).ifPresent(cap -> {
            String currentAmmoKey = cap.getAmmoVariantInGun(stack);
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
