package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.model.EquipmentTarget;

public record SelectEquipmentMountPacket(int containerId, EquipmentTarget target, ResourceLocation mountId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectEquipmentMountPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "select_equipment_mount"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectEquipmentMountPacket> STREAM_CODEC =
            StreamCodec.ofMember(SelectEquipmentMountPacket::encode, SelectEquipmentMountPacket::decode);

    @Override
    public CustomPacketPayload.Type<SelectEquipmentMountPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        buf.writeResourceLocation(mountId);
    }

    public static SelectEquipmentMountPacket decode(RegistryFriendlyByteBuf buf) {
        return new SelectEquipmentMountPacket(buf.readVarInt(), EquipmentTarget.read(buf), buf.readResourceLocation());
    }

    public static void handle(SelectEquipmentMountPacket packet, IPayloadContext context) {
        // Registered with HandlerThread.MAIN.
        var player = context.player() instanceof ServerPlayer sender ? sender : null;
        if (player != null && player.containerMenu instanceof EquipmentMenu menu
                && menu.containerId == packet.containerId()) {
            menu.selectAnchor(packet.target(), packet.mountId());
        }
    }
}
