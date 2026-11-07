package net.zerocontact.armor.modular.module.nvg.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;
import net.zerocontact.capability.CapabilityRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BatteryStatusOverlay {
    public static final IGuiOverlay NVG_BATTERY = (forgeGui, graphics, partialTick, w, h) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.options.hideGui || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player) return;
        float alpha = ClientInteractionManger.get().vignetteAlpha(partialTick);
        ItemStack checkStack = ClientInteractionManger.get().module();
        if (checkStack.getItem() instanceof INvg) {
            checkStack.getCapability(CapabilityRegistries.NVG).ifPresent(cap -> {
                String battery = "Power";
                int fontWidth = minecraft.font.width(battery);
                int barWidth = 12;
                int maxHeight = 18;
                int screenMargin = 4;
                // Include the label's shadow when checking the screen boundary.
                int overlayWidth = Math.max(barWidth, fontWidth + 1);
                int hotbarRight = w / 2 + 91;
                // Reserve space for an offhand slot or the hotbar attack indicator.
                int overlayLeft = hotbarRight;
                int heightY = h - 2;
                if (overlayLeft + overlayWidth > w - screenMargin) {
                    overlayLeft = Math.max(screenMargin,
                            Math.min(hotbarRight - overlayWidth, w - screenMargin - overlayWidth));
                    // Stay above the hotbar and the current health/armor/air rows.
                    heightY = h - Math.max(22, Math.max(forgeGui.leftHeight, forgeGui.rightHeight)) - screenMargin;
                }
                int startX = overlayLeft + (overlayWidth - barWidth) / 2;
                int widthX = startX + barWidth;
                int labelX = overlayLeft + (overlayWidth - fontWidth) / 2;
                int labelY = heightY - maxHeight - minecraft.font.lineHeight - 2;
                float maxBattery = cap.getMaxBattery();
                float remainBatteryPercent = maxBattery > 0
                        ? Math.max(0f, Math.min(1f, cap.getBattery() / maxBattery))
                        : 0f;
                int startY = heightY - Math.round(maxHeight * remainBatteryPercent);
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionShader);
                RenderSystem.setShaderColor(1, 1, 1, alpha);
                graphics.fill(startX, startY, widthX, heightY, 0xffffffff);
                graphics.fill(startX, startY, widthX, heightY - maxHeight, 0xaaaaaaaa);
                graphics.drawString(minecraft.font, battery, labelX, labelY, 0xffffff);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
            });

        }
    };

    @SubscribeEvent
    public static void onRegisterOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("zc_nvg_battery", NVG_BATTERY);
    }
}
