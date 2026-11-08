package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.model.EquipmentTarget;

import java.util.function.Supplier;

public record UnMountPacket(int containerId, EquipmentTarget target, ResourceLocation mountId) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        buf.writeResourceLocation(mountId);
    }

    public static UnMountPacket decode(FriendlyByteBuf buf) {
        return new UnMountPacket(buf.readVarInt(), EquipmentTarget.read(buf), buf.readResourceLocation());
    }

    public static void handle(UnMountPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var player = supplier.get().getSender();
        if (player != null && player.containerMenu instanceof EquipmentMenu menu
                && menu.containerId == packet.containerId()) {
            menu.unMount(packet.target(), packet.mountId());
        }
        supplier.get().setPacketHandled(true);
    }
}
