package net.zerocontact.armor.modular.module.nvg.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;

import java.util.Optional;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public final class BatteryStatusOverlay {
    public static final LayeredDraw.Layer NVG_BATTERY = (forgeGui, deltaTracker) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.options.hideGui || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player) return;
        float alpha = ClientInteractionManger.get().vignetteAlpha(deltaTracker.getGameTimeDeltaPartialTick(false));
        ItemStack checkStack = ClientInteractionManger.get().module();
        if (checkStack.getItem() instanceof INvg) {
            Optional.ofNullable(checkStack.getCapability(ModularCapabilityRegistry.NVG)).ifPresent(cap -> {
                String battery = "Power";
                int fontWidth = minecraft.font.width(battery);
                int barWidth = 12;
                int maxHeight = 18;
                int screenMargin = 4;
                // Include the label's shadow when checking the screen boundary.
                int overlayWidth = Math.max(barWidth, fontWidth + 1);
                int hotbarRight = forgeGui.guiWidth() / 2 + 91;
                // Reserve space for an offhand slot or the hotbar attack indicator.
                int overlayLeft = hotbarRight;
                int heightY = forgeGui.guiHeight() - 2;
                if (overlayLeft + overlayWidth > forgeGui.guiWidth() - screenMargin) {
                    overlayLeft = Math.clamp(hotbarRight - overlayWidth, screenMargin,
                            Math.max(screenMargin, forgeGui.guiWidth() - screenMargin - overlayWidth));
                    // Stay above the hotbar and the current health/armor/air rows.
                    heightY = forgeGui.guiHeight() - Math.max(22, Math.max(minecraft.gui.leftHeight, minecraft.gui.rightHeight)) - screenMargin;
                }
                int startX = overlayLeft + (overlayWidth - barWidth) / 2;
                int widthX = startX + barWidth;
                int labelX = overlayLeft + (overlayWidth - fontWidth) / 2;
                int labelY = heightY - maxHeight - minecraft.font.lineHeight - 2;
                float maxBattery = cap.getMaxBattery();
                float remainBatteryPercent = maxBattery > 0
                        ? Math.clamp(cap.getBattery() / maxBattery, 0f, 1f)
                        : 0f;
                int startY = heightY - Math.round(maxHeight * remainBatteryPercent);
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionShader);
                RenderSystem.setShaderColor(1, 1, 1, alpha);
                forgeGui.fill(startX, startY, widthX, heightY, 0xffffffff);
                forgeGui.fill(startX, startY, widthX, heightY - maxHeight, 0xaaaaaaaa);
                forgeGui.drawString(minecraft.font, battery, labelX, labelY, 0xffffff);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
            });

        }
    };

    @SubscribeEvent
    public static void onRegisterOverlay(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "zc_nvg_battery"), NVG_BATTERY);
    }
}
