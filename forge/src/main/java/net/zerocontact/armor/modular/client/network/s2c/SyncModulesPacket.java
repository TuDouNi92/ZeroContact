package net.zerocontact.armor.modular.client.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.armor.modular.client.network.handler.ModuleSyncHandler;

import java.util.function.Supplier;

/** Full module snapshot for one equipped stack, including an empty container. */
public record SyncModulesPacket(int entityId, EquipmentTarget target, ResourceLocation itemId, CompoundTag modules) {
    public SyncModulesPacket {
        modules = modules.copy();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        target.write(buf);
        buf.writeResourceLocation(itemId);
        buf.writeNbt(modules);
    }

    public static SyncModulesPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        EquipmentTarget target = EquipmentTarget.read(buf);
        ResourceLocation itemId = buf.readResourceLocation();
        CompoundTag modules = buf.readNbt();
        return new SyncModulesPacket(entityId, target, itemId, modules == null ? new CompoundTag() : modules);
    }

    public static void handle(SyncModulesPacket packet, Supplier<NetworkEvent.Context> supplier) {
        // Registered with consumerMainThread: already dispatched on the client thread.
        ModuleSyncHandler.handle(packet);
        supplier.get().setPacketHandled(true);
    }
}
