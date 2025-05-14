package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.ClientStaminaData;
import net.zerocontact.stamina.PlayerStaminaProvider;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class StaminaHUD {
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;
        if (localPlayer == null) return;
        localPlayer.getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(playerStamina -> {
            float maxStamina = 120f;
            float stamina = ClientStaminaData.getStamina();
            float percent = stamina / maxStamina;
            int barWidth = 100;
            int barHeight = 2;
            int x = mc.getWindow().getGuiScaledWidth() / 2 - barWidth / 2;
            int y = mc.getWindow().getGuiScaledHeight() - 40;
            int fillWidth = (int) Math.floor(barWidth * percent);
            GuiGraphics graphics = event.getGuiGraphics();
            graphics.fill(x, y, x + barWidth, y + barHeight, 0x885B5B5B);
            graphics.fill(x, y, x + fillWidth, y + barHeight, 0x88FFFFFF);
            graphics.drawString(mc.font, "stamina:" + stamina, x, y - 10, 0xFFFFFFFF);
        });
    }

}
