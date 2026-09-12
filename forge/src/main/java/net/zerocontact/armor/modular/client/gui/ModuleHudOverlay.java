package net.zerocontact.armor.modular.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModuleHudOverlay {
    public static final IGuiOverlay MODULE_HUD =
            (gui, graphics, partialTick, width, height) -> {
                renderModules(graphics, width, height);
            };

    private static void renderModules(GuiGraphics graphics, int screenWidth, int screenHeight) {

        Minecraft mc = Minecraft.getInstance();
        ModuleHudState state = ModuleHudState.INSTANCE;
        if (!state.isOpened() || mc.player == null || mc.screen != null || mc.options.hideGui) return;
        var entries = state.entries();
        int top = 8;
        int rowHeight = 20;
        int width = Math.min(200, Math.max(1, screenWidth - 16));
        int left = (screenWidth - width) / 2;
        int visible = Math.min(8, Math.max(1, (screenHeight - top - 12) / rowHeight));
        int rows = Math.max(1, Math.min(visible, entries.size()));
        int first = Math.max(0, Math.min(state.selectedIndex() - visible / 2, entries.size() - visible));
        int bottom = top + rows * rowHeight + 4;
        graphics.fill(left + 2, top + 3, left + width + 2, bottom + 3, 0x55000000);
        graphics.fill(left, top, left + width, bottom, 0xEE171B20);
        graphics.renderOutline(left, top, width, bottom - top, 0xFF4B535D);
        if (entries.isEmpty()) {
            graphics.drawCenteredString(mc.font, Component.translatable("hud.zerocontact.modules.empty"),
                    screenWidth / 2, top + 7, 0xFF9CA5AE);
            return;
        }
        graphics.enableScissor(left + 1, top + 1, left + width - 1, bottom - 1);
        try {
            for (int row = 0; row < rows; row++) {
                int index = first + row;
                var entry = entries.get(index);
                int y = top + 2 + row * rowHeight;
                boolean selected = index == state.selectedIndex();
                if (selected) {
                    graphics.fill(left + 2, y, left + width - 2, y + rowHeight, 0xFF303E4B);
                    graphics.fill(left + 2, y + 3, left + 4, y + rowHeight - 3, 0xFF91BDD9);
                }
                String status = fit(entry.stateLabel().getString(), Math.min(64, width / 3));
                int statusWidth = mc.font.width(status);
                String name = fit(entry.name().getString(), Math.max(0, width - statusWidth - 28));
                graphics.drawString(mc.font, name, left + 9, y + 6, 0xFFE7EBEF, false);
                graphics.drawString(mc.font, status, left + width - 9 - statusWidth, y + 6,
                        selected ? 0xFFB9D9CD : 0xFF9DA8B3, false);
            }
            if (entries.size() > visible) {
                int track = rows * rowHeight;
                int thumb = Math.max(5, track * visible / entries.size());
                int y = top + 2 + (track - thumb) * first / (entries.size() - visible);
                graphics.fill(left + width - 3, y, left + width - 2, y + thumb, 0xFF8494A4);
            }
        } finally {
            graphics.disableScissor();
        }
    }

    private static String fit(String text, int width) {
        var font = Minecraft.getInstance().font;
        if (font.width(text) <= width) return text;
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }


    @SubscribeEvent
    public static void registerOverlayEvent(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("module_menu", MODULE_HUD);
    }


}
