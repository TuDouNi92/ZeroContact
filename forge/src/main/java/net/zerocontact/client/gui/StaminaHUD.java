package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.ClientData;
import net.zerocontact.stamina.PlayerStaminaProvider;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class StaminaHUD {
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;
        if (localPlayer == null) return;
        localPlayer.getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(playerStamina -> drawStamina(event, mc));
    }

    private static void drawStamina(RenderGuiOverlayEvent.Post event, Minecraft mc) {
        if(!ClientData.isEnableStamina())return;
        float maxStamina = 120f;
        float stamina = ClientData.getStamina();
        float percent = stamina / maxStamina;
        int barWidth = 100;
        int barHeight = 2;
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() - 40;
        int fillWidth = (int) Math.floor(barWidth * percent);
        int fillHalfWidth = fillWidth/2;
        int leftX = centerX -fillHalfWidth;
        int rightX = centerX+fillHalfWidth;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(centerX-barWidth/2, y, centerX + barWidth/2, y + barHeight, 0x885B5B5B);
        graphics.fill(leftX, y, rightX, y + barHeight, 0x88FFFFFF);
//        graphics.drawString(mc.font, "stamina:" + stamina, centerX-40, y - 10, 0xFFFFFFFF);
    }

}
