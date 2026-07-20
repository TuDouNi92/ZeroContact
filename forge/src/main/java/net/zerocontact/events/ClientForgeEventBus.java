package net.zerocontact.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientForgeEventBus {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) return;
        listenVisorKey();
        listenBackpackKey();
        listenAmmoSelectorKey();
    }

    private static void listenAmmoSelectorKey(){
        while (KeyBindingHandler.TOGGLE_AMMO_SELECTOR.consumeClick()){
            if(Minecraft.getInstance().screen == null){
                ModMessages.sendToServer(new NetworkHandler.OpenAmmoSelectorPacket());
            }
        }
    }
    private static void listenBackpackKey() {
        while (KeyBindingHandler.TOGGLE_BACKPACK_KEY.consumeClick()) {
            if (Minecraft.getInstance().screen == null) {
                ModMessages.sendToServer(new NetworkHandler.ToggleBackpackPacket(true));
            }
        }
    }

    private static void listenVisorKey() {
        if (KeyBindingHandler.TOGGLE_VISOR_KEY.consumeClick()) {
            ModMessages.sendToServer(new NetworkHandler.FlipVisorPacket());
        }
    }
}
