package net.zerocontact.network.s2c;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.client.network.ClientPacketHandlers;

public class SyncStaminaPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncStaminaPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "sync_stamina"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStaminaPacket> STREAM_CODEC =
            StreamCodec.ofMember(SyncStaminaPacket::toBytes, SyncStaminaPacket::new);

    @Override
    public CustomPacketPayload.Type<SyncStaminaPacket> type() {
        return TYPE;
    }

    private final float stamina;
    private final boolean enabled;

    public SyncStaminaPacket(float stamina, boolean enabled) {
        this.stamina = stamina;
        this.enabled = enabled;
    }

    public SyncStaminaPacket(RegistryFriendlyByteBuf buf) {
        this.stamina = buf.readFloat();
        this.enabled = buf.readBoolean();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(stamina);
        buf.writeBoolean(enabled);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandlers.syncStamina(stamina, enabled));
    }
}
