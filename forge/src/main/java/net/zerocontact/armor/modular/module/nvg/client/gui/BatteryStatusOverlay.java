package net.zerocontact.armor.modular.module.nvg.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
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
                int startX = w / 2 + w / 5;
                int widthX = startX + 12;
                int heightY = h - 2;
                int maxHeight = 18;
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
                String battery = "Power";
                int fontWidth = minecraft.font.width(battery);
                graphics.fill(startX, startY, widthX, heightY, 0xffffffff);
                graphics.fill(startX, startY, widthX, heightY - maxHeight, 0xaaaaaaaa);
                graphics.drawString(minecraft.font, battery, startX - fontWidth/3, heightY - maxHeight -8, 0xffffff);
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
