package net.zerocontact.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickEvent {
    private static boolean lastPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) return;
        toggleVisorKey();
        toggleBackPackKey();
    }

    private static void toggleBackPackKey() {
        while (KeyBindingHandler.TOGGLE_BACKPACK_KEY.consumeClick()) {
            if (Minecraft.getInstance().screen == null && !ClientData.justCloseBackpack) {
                ModMessages.sendToServer(new NetworkHandler.ToggleBackpackPacket(true));
            }
        }
        ClientData.justCloseBackpack = false;
    }

    private static void toggleVisorKey() {
        if (toggle(KeyBindingHandler.TOGGLE_VISOR_KEY.isDown())) {
            ClientData.setTriggerViosorToggle(true);
            ModMessages.sendToServer(new NetworkHandler.ToggleVisorPacket(ClientData.isTriggerVisorToggle()));
        } else {
            ClientData.setTriggerViosorToggle(false);
        }
    }

    public static boolean toggle(boolean keyPressedDown) {
        boolean canToggle = keyPressedDown && !lastPressed;
        lastPressed = keyPressedDown;
        return canToggle;
    }
}
