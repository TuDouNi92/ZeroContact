package net.zerocontact.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.caliber.ServerAmmoSelector;

public record OpenAmmoSelectorPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenAmmoSelectorPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "open_ammo_selector"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAmmoSelectorPacket> STREAM_CODEC =
            StreamCodec.ofMember(OpenAmmoSelectorPacket::encode, OpenAmmoSelectorPacket::decode);

    @Override
    public CustomPacketPayload.Type<OpenAmmoSelectorPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
    }

    public static OpenAmmoSelectorPacket decode(RegistryFriendlyByteBuf buf) {
        return new OpenAmmoSelectorPacket();
    }

    public static void handle(OpenAmmoSelectorPacket msg, IPayloadContext context) {
        ServerAmmoSelector.handleMenu(msg, context);
    }
}
