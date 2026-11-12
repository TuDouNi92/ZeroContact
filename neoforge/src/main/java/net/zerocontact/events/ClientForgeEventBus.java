package net.zerocontact.events;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.client.network.c2s.OpenModularMenuPacket;
import net.zerocontact.armor.modular.client.screen.EquipmentScreen;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.c2s.FlipVisorPacket;
import net.zerocontact.network.c2s.OpenAmmoSelectorPacket;
import net.zerocontact.network.c2s.ToggleBackpackPacket;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public class ClientForgeEventBus {
    private static boolean suppressBackpackOpenUntilKeyRelease;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;
        listenVisorKey();
        listenBackpackKey();
        listenAmmoSelectorKey();
        listenEquipmentMenuKey();
    }

    private static void listenAmmoSelectorKey() {
        while (KeyBindingHandler.TOGGLE_AMMO_SELECTOR.consumeClick()) {
            if (Minecraft.getInstance().screen == null) {
                ModMessages.sendToServer(new OpenAmmoSelectorPacket());
            }
        }
    }

    private static void listenEquipmentMenuKey() {
        while (KeyBindingHandler.TOGGLE_MODULAR_MENU.consumeClick()) {
            if (Minecraft.getInstance().screen == null) {
                ModMessages.sendToServer(new OpenModularMenuPacket());
            }
        }
    }

    private static void listenBackpackKey() {
        while (KeyBindingHandler.TOGGLE_BACKPACK_KEY.consumeClick()) {
            if (!suppressBackpackOpenUntilKeyRelease && Minecraft.getInstance().screen == null) {
                ModMessages.sendToServer(new ToggleBackpackPacket(true));
            }
        }

        if (suppressBackpackOpenUntilKeyRelease && !KeyBindingHandler.TOGGLE_BACKPACK_KEY.isDown()) {
            suppressBackpackOpenUntilKeyRelease = false;
        }
    }

    public static void suppressBackpackOpenUntilKeyRelease() {
        suppressBackpackOpenUntilKeyRelease = true;
    }

    private static void listenVisorKey() {
        if (KeyBindingHandler.TOGGLE_VISOR_KEY.consumeClick()) {
            ModMessages.sendToServer(new FlipVisorPacket());
        }
    }

}
