package net.zerocontact.network.s2c;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.client.network.ClientAmmoReloadHandler;

public record ClientAmmoReloadPacket(int gunSlot, String selectedAmmoKey) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientAmmoReloadPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "client_ammo_reload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientAmmoReloadPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientAmmoReloadPacket::encode, ClientAmmoReloadPacket::decode);

    @Override
    public CustomPacketPayload.Type<ClientAmmoReloadPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(gunSlot);
        buf.writeUtf(selectedAmmoKey);
    }

    public static ClientAmmoReloadPacket decode(RegistryFriendlyByteBuf buf) {
        return new ClientAmmoReloadPacket(buf.readInt(), buf.readUtf());
    }

    public static void handle(ClientAmmoReloadPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientAmmoReloadHandler.handle(packet);
            }
        });
    }
}
