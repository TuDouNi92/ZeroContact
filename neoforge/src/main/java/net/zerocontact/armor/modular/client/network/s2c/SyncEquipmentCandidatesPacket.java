package net.zerocontact.armor.modular.client.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.client.network.ItemStackCodec;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu.Candidate;
import net.zerocontact.armor.modular.client.network.handler.EquipmentCandidatesHandler;

import java.util.List;
import java.util.function.Supplier;

public record SyncEquipmentCandidatesPacket(int containerId, EquipmentTarget target, ItemStack equipment,
                                            ResourceLocation mountId, List<Candidate> candidates) {
    public SyncEquipmentCandidatesPacket {
        equipment = equipment.copy();
        candidates = candidates.stream()
                .map(candidate -> new Candidate(candidate.inventorySlot(), candidate.stack())).toList();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        target.write(buf);
        buf.writeItem(equipment);
        buf.writeResourceLocation(mountId);
        buf.writeCollection(candidates, (buffer, candidate) -> {
            buffer.writeVarInt(candidate.inventorySlot());
            ItemStackCodec.write(buf, candidate.stack());
        });
    }

    public static SyncEquipmentCandidatesPacket decode(FriendlyByteBuf buf) {
        return new SyncEquipmentCandidatesPacket(buf.readVarInt(), EquipmentTarget.read(buf), buf.readItem(), buf.readResourceLocation(),
                buf.readList(buffer -> new Candidate(buffer.readVarInt(), ItemStackCodec.read(buf))));
    }

    public static void handle(SyncEquipmentCandidatesPacket packet, Supplier<NetworkEvent.Context> supplier) {
        // Registered with consumerMainThread.
        EquipmentCandidatesHandler.handle(packet);
        supplier.get().setPacketHandled(true);
    }
}
