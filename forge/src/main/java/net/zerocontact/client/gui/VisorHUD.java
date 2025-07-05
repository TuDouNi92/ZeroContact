package net.zerocontact.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.ClientData;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class VisorHUD {
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean success = ClientData.isLastToggleVisorEnabled();
        if (success) {
            drawHUD(event, mc);
        }
    }

    private static void drawHUD(RenderGuiOverlayEvent.Pre event, Minecraft mc) {
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (mc.player == null) return;
        if (mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Toggleable toggleable){
            RenderSystem.enableBlend();
            graphics.blit(toggleable.getVisorTexture(),0,0,-100,0,0,width,height,width,height);
            RenderSystem.disableBlend();
        }
    }
}
