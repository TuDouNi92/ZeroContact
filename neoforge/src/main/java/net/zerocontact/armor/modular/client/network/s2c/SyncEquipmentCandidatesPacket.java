package net.zerocontact.armor.modular.client.network.s2c;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.client.network.ItemStackCodec;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu.Candidate;
import net.zerocontact.armor.modular.client.network.handler.EquipmentCandidatesHandler;

import java.util.List;

public record SyncEquipmentCandidatesPacket(int containerId, EquipmentTarget target, ItemStack equipment,
                                            ResourceLocation mountId, List<Candidate> candidates) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncEquipmentCandidatesPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "sync_equipment_candidates"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEquipmentCandidatesPacket> STREAM_CODEC =
            StreamCodec.ofMember(SyncEquipmentCandidatesPacket::encode, SyncEquipmentCandidatesPacket::decode);

    @Override
    public CustomPacketPayload.Type<SyncEquipmentCandidatesPacket> type() {
        return TYPE;
    }

    public SyncEquipmentCandidatesPacket {
        equipment = equipment.copy();
        candidates = candidates.stream()
                .map(candidate -> new Candidate(candidate.inventorySlot(), candidate.stack())).toList();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        ItemStackCodec.write(buf, equipment);
        buf.writeResourceLocation(mountId);
        buf.writeCollection(candidates, (buffer, candidate) -> {
            buffer.writeVarInt(candidate.inventorySlot());
            ItemStackCodec.write(buf, candidate.stack());
        });
    }

    public static SyncEquipmentCandidatesPacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncEquipmentCandidatesPacket(buf.readVarInt(), EquipmentTarget.read(buf), ItemStackCodec.read(buf), buf.readResourceLocation(),
                buf.readList(buffer -> new Candidate(buffer.readVarInt(), ItemStackCodec.read(buf))));
    }

    public static void handle(SyncEquipmentCandidatesPacket packet, IPayloadContext context) {
        // Registered with HandlerThread.MAIN.
        EquipmentCandidatesHandler.handle(packet);
    }
}
