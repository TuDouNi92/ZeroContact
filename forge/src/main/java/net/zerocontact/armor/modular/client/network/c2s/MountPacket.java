package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.model.EquipmentTarget;

import java.util.function.Supplier;

public record MountPacket(int containerId, EquipmentTarget target, ResourceLocation mountId,
                          int inventorySlot, ItemStack moduleStack) {
    public MountPacket {
        moduleStack = moduleStack.copy();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeVarInt(inventorySlot);
        buf.writeItem(moduleStack);
    }

    public static MountPacket decode(FriendlyByteBuf buf) {
        return new MountPacket(
                buf.readVarInt(),
                EquipmentTarget.read(buf),
                buf.readResourceLocation(),
                buf.readVarInt(),
                buf.readItem()
        );
    }

    public static void handle(MountPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var player = supplier.get().getSender();
        if (player != null && player.containerMenu instanceof EquipmentMenu menu
                && menu.containerId == packet.containerId()) {
            menu.mountCandidate(packet.target(), packet.mountId(), packet.inventorySlot(), packet.moduleStack());
        }
        supplier.get().setPacketHandled(true);
    }
}
