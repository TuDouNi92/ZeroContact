package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.client.network.ItemStackCodec;
import net.zerocontact.armor.modular.model.EquipmentTarget;

public record MountPacket(int containerId, EquipmentTarget target, ResourceLocation mountId,
                          int inventorySlot, ItemStack moduleStack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MountPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "mount"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MountPacket> STREAM_CODEC =
            StreamCodec.ofMember(MountPacket::encode, MountPacket::decode);

    @Override
    public CustomPacketPayload.Type<MountPacket> type() {
        return TYPE;
    }

    public MountPacket {
        moduleStack = moduleStack.copy();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeVarInt(inventorySlot);
        ItemStackCodec.write(buf, moduleStack);
    }

    public static MountPacket decode(RegistryFriendlyByteBuf buf) {
        return new MountPacket(
                buf.readVarInt(),
                EquipmentTarget.read(buf),
                buf.readResourceLocation(),
                buf.readVarInt(),
                ItemStackCodec.read(buf)
        );
    }

    public static void handle(MountPacket packet, IPayloadContext context) {
        var player = context.player() instanceof ServerPlayer sender ? sender : null;
        if (player != null && player.containerMenu instanceof EquipmentMenu menu
                && menu.containerId == packet.containerId()) {
            menu.mountCandidate(packet.target(), packet.mountId(), packet.inventorySlot(), packet.moduleStack());
        }
    }
}
