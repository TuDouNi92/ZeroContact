package net.zerocontact.client.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindingHandler {
    public static KeyMapping TOGGLE_VISOR_KEY;
    public static void register(RegisterKeyMappingsEvent event){
        TOGGLE_VISOR_KEY = new KeyMapping("key.swap_visor", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X,"key.categories.zerocontact");
        event.register(TOGGLE_VISOR_KEY);
    }
}
