package net.zerocontact.armor.modular.module.nvg.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;

@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DuoVignetteOverlay {
    public static final IGuiOverlay DUO_VIGNETTE = (forgeGui, graphics, partialTick, w, h) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.options.hideGui || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player) return;
        float alpha = ClientInteractionManger.get().vignetteAlpha(partialTick);
        if (alpha <= 0) return;
        int color = Math.round(255 * alpha) << 24;
        // Two circular apertures, drawn with GuiGraphics to avoid missing placeholder textures
        // and manual global RenderSystem state changes. Radius is measured in GUI pixels.
        double radius = Math.min(h * 0.48, w * 0.32);
        double leftCenter = w * 0.5 - radius * 0.52;
        double rightCenter = w * 0.5 + radius * 0.52;
        for (int y = 0; y < h; y++) {
            double dy = y + 0.5 - h * 0.5;
            if (Math.abs(dy) >= radius) {
                graphics.fill(0, y, w, y + 1, color);
                continue;
            }
            double half = Math.sqrt(radius * radius - dy * dy);
            int left = Math.max(0, (int) Math.floor(leftCenter - half));
            int right = Math.min(w, (int) Math.ceil(rightCenter + half));
            graphics.fill(0, y, left, y + 1, color);
            graphics.fill(right, y, w, y + 1, color);
            int gapLeft = (int) Math.ceil(leftCenter + half);
            int gapRight = (int) Math.floor(rightCenter - half);
            if (gapLeft < gapRight) graphics.fill(gapLeft, y, gapRight, y + 1, color);
        }
    };

    @SubscribeEvent
    public static void onRegisterOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("zc_duo_nvg_vignette", DUO_VIGNETTE);
    }
}
