package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.model.EquipmentTarget;

public record UnMountPacket(int containerId, EquipmentTarget target, ResourceLocation mountId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UnMountPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "un_mount"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnMountPacket> STREAM_CODEC =
            StreamCodec.ofMember(UnMountPacket::encode, UnMountPacket::decode);

    @Override
    public CustomPacketPayload.Type<UnMountPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        buf.writeResourceLocation(mountId);
    }

    public static UnMountPacket decode(RegistryFriendlyByteBuf buf) {
        return new UnMountPacket(buf.readVarInt(), EquipmentTarget.read(buf), buf.readResourceLocation());
    }

    public static void handle(UnMountPacket packet, IPayloadContext context) {
        var player = context.player() instanceof ServerPlayer sender ? sender : null;
        if (player != null && player.containerMenu instanceof EquipmentMenu menu
                && menu.containerId == packet.containerId()) {
            menu.unMount(packet.target(), packet.mountId());
        }
    }
}
