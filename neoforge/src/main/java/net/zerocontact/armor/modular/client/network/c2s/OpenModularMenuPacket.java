package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;

import java.util.function.Supplier;

public record OpenModularMenuPacket() {
    public void encode(FriendlyByteBuf __) {
    }

    public static OpenModularMenuPacket decode(FriendlyByteBuf __) {
        return new OpenModularMenuPacket();
    }

    public static void handle(OpenModularMenuPacket __, Supplier<NetworkEvent.Context> supplier) {
        // Registered with consumerMainThread.
        var player = supplier.get().getSender();
        if (player != null) {
            // Use the shared opener: the client constructor requires its extra data payload.
            EquipmentMenu.open(player, Component.translatable("screen.zerocontact.equipment.title"));
        }
        supplier.get().setPacketHandled(true);
    }
}
