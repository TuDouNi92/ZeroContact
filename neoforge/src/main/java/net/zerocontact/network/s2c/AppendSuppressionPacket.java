package net.zerocontact.network.s2c;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.client.network.ClientPacketHandlers;
import org.joml.Vector3f;

public record AppendSuppressionPacket(Vector3f bulletPos, float amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AppendSuppressionPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "append_suppression"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AppendSuppressionPacket> STREAM_CODEC =
            StreamCodec.ofMember(AppendSuppressionPacket::encode, AppendSuppressionPacket::decode);

    @Override
    public CustomPacketPayload.Type<AppendSuppressionPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVector3f(bulletPos);
        buf.writeFloat(amount);
    }

    public static AppendSuppressionPacket decode(RegistryFriendlyByteBuf buf) {
        return new AppendSuppressionPacket(buf.readVector3f(), buf.readFloat());
    }

    public static void handle(AppendSuppressionPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandlers.appendSuppression(msg));
    }
}
