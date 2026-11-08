package net.zerocontact.armor.modular.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.KeyBindingHandler;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModuleHudInputHandler {
    private static boolean menuHeld;
    private static boolean actionHeld;
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var menu = KeyBindingHandler.TOGGLE_MODULE_HUD;
        var action = KeyBindingHandler.ACTIVATE_MODULE;
        if (menu == null || action == null) return;
        boolean menuPressed = false;
        boolean actionPressed = false;
        while (menu.consumeClick()) menuPressed = true;
        while (action.consumeClick()) actionPressed = true;
        Minecraft mc = Minecraft.getInstance();
        ModuleHudState state = ModuleHudState.INSTANCE;
        if (mc.player == null || mc.level == null || mc.screen != null || !mc.isWindowActive()) {
            state.close();
        } else {
            if (menuPressed && !menuHeld) state.toggle();
            if (state.isOpened()) {
                state.refresh();
                if (actionPressed && !actionHeld) state.activate();
            }
        }
        menuHeld = menu.isDown();
        actionHeld = action.isDown();
    }
    @SubscribeEvent
    public static void scroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!ModuleHudState.INSTANCE.isOpened() || mc.player == null || mc.screen != null) return;
        event.setCanceled(true);
        if (event.getScrollDelta() != 0) {
            ModuleHudState.INSTANCE.move(event.getScrollDelta() > 0 ? -1 : 1);
        }
    }
}
