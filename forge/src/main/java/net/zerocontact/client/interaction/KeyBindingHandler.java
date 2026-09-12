package net.zerocontact.client.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class KeyBindingHandler {
    public static KeyMapping TOGGLE_BACKPACK_KEY;
    public static KeyMapping TOGGLE_VISOR_KEY;
    public static KeyMapping TOGGLE_AMMO_SELECTOR;
    public static KeyMapping TOGGLE_MODULAR_MENU;
    public static KeyMapping TOGGLE_MODULE_HUD;
    public static KeyMapping ACTIVATE_MODULE;

    public static void register(RegisterKeyMappingsEvent event) {
        TOGGLE_VISOR_KEY = new KeyMapping("key.swap_visor", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.zerocontact");
        TOGGLE_BACKPACK_KEY = new KeyMapping("key.open_backpack", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.zerocontact");
        TOGGLE_AMMO_SELECTOR = new KeyMapping("key.open_selector", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.zerocontact");
        TOGGLE_MODULAR_MENU = new KeyMapping("key.open_modular_menu", KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.zerocontact");
        event.register(TOGGLE_VISOR_KEY);
        event.register(TOGGLE_BACKPACK_KEY);
        event.register(TOGGLE_AMMO_SELECTOR);
        event.register(TOGGLE_MODULAR_MENU);
        TOGGLE_MODULE_HUD = new KeyMapping("key.zerocontact.module_hud", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.zerocontact");
        ACTIVATE_MODULE = new KeyMapping("key.zerocontact.module_activate", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.zerocontact");
        event.register(TOGGLE_MODULE_HUD);
        event.register(ACTIVATE_MODULE);
    }

}
