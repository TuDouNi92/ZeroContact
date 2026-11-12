package net.zerocontact.armor.modular.client.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.network.handler.ModuleSyncHandler;

/** Full module snapshot for one equipped stack, including an empty container. */
public record SyncModulesPacket(int entityId, EquipmentTarget target, ResourceLocation itemId, CompoundTag modules) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncModulesPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "sync_modules"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncModulesPacket> STREAM_CODEC =
            StreamCodec.ofMember(SyncModulesPacket::encode, SyncModulesPacket::decode);

    @Override
    public CustomPacketPayload.Type<SyncModulesPacket> type() {
        return TYPE;
    }

    public SyncModulesPacket {
        modules = modules.copy();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        target.write(buf);
        buf.writeResourceLocation(itemId);
        buf.writeNbt(modules);
    }

    public static SyncModulesPacket decode(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        EquipmentTarget target = EquipmentTarget.read(buf);
        ResourceLocation itemId = buf.readResourceLocation();
        CompoundTag modules = buf.readNbt();
        return new SyncModulesPacket(entityId, target, itemId, modules == null ? new CompoundTag() : modules);
    }

    public static void handle(SyncModulesPacket packet, IPayloadContext context) {
        // Registered with HandlerThread.MAIN: already dispatched on the client thread.
        ModuleSyncHandler.handle(packet);
    }
}
