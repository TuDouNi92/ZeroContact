package net.zerocontact.armor.modular.client.network.s2c;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.network.handler.NavBoardUpdateHandler;
import net.zerocontact.armor.modular.model.EquipmentTarget;

/** Requests local sampling; contains no terrain or pixel data. */
public record UpdateNavBoardPacket(EquipmentTarget target, ResourceLocation mountId,
                                   ResourceLocation equipmentItem, ResourceLocation moduleItem,
                                   ResourceLocation dimension) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateNavBoardPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "update_nav_board"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNavBoardPacket> STREAM_CODEC =
            StreamCodec.ofMember(UpdateNavBoardPacket::encode, UpdateNavBoardPacket::decode);

    @Override
    public CustomPacketPayload.Type<UpdateNavBoardPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeResourceLocation(equipmentItem);
        buf.writeResourceLocation(moduleItem);
        buf.writeResourceLocation(dimension);
    }

    public static UpdateNavBoardPacket decode(RegistryFriendlyByteBuf buf) {
        return new UpdateNavBoardPacket(EquipmentTarget.read(buf), buf.readResourceLocation(),
                buf.readResourceLocation(), buf.readResourceLocation(), buf.readResourceLocation());
    }

    public static void handle(UpdateNavBoardPacket packet, IPayloadContext context) {
        // Registered with HandlerThread.MAIN, matching the existing module packets.
        NavBoardUpdateHandler.handle(packet);
    }
}
