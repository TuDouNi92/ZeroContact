package net.zerocontact.network.s2c;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.client.network.ClientPacketHandlers;

public record ToggleVisorResultPacket(AnimateData.VisorAnimateData visorAnimateData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleVisorResultPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "toggle_visor_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleVisorResultPacket> STREAM_CODEC =
            StreamCodec.ofMember(ToggleVisorResultPacket::encode, ToggleVisorResultPacket::decode);

    @Override
    public CustomPacketPayload.Type<ToggleVisorResultPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        String animName = "";
        boolean isPlaying = false;
        double animLength = 0;
        if (visorAnimateData != null) {
            animName = visorAnimateData.animationName;
            isPlaying = visorAnimateData.isPlaying;
            animLength = visorAnimateData.animLength;
        }
        buf.writeUtf(animName);
        buf.writeDouble(animLength);
        buf.writeBoolean(isPlaying);
    }

    public static ToggleVisorResultPacket decode(RegistryFriendlyByteBuf buf) {
        return new ToggleVisorResultPacket(
                new AnimateData.VisorAnimateData(
                        buf.readUtf(),
                        buf.readDouble(),
                        buf.readBoolean()
                )
        );
    }

    public static void handle(ToggleVisorResultPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandlers.toggleVisor(msg));
    }
}
