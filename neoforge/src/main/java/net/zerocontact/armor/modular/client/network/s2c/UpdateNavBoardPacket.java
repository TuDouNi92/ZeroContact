package net.zerocontact.armor.modular.client.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.armor.modular.client.network.handler.NavBoardUpdateHandler;
import net.zerocontact.armor.modular.model.EquipmentTarget;

import java.util.function.Supplier;

/** Requests local sampling; contains no terrain or pixel data. */
public record UpdateNavBoardPacket(EquipmentTarget target, ResourceLocation mountId,
                                   ResourceLocation equipmentItem, ResourceLocation moduleItem,
                                   ResourceLocation dimension) {
    public void encode(FriendlyByteBuf buf) {
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeResourceLocation(equipmentItem);
        buf.writeResourceLocation(moduleItem);
        buf.writeResourceLocation(dimension);
    }

    public static UpdateNavBoardPacket decode(FriendlyByteBuf buf) {
        return new UpdateNavBoardPacket(EquipmentTarget.read(buf), buf.readResourceLocation(),
                buf.readResourceLocation(), buf.readResourceLocation(), buf.readResourceLocation());
    }

    public static void handle(UpdateNavBoardPacket packet, Supplier<NetworkEvent.Context> context) {
        // Registered with consumerMainThread, matching the existing module packets.
        NavBoardUpdateHandler.handle(packet);
        context.get().setPacketHandled(true);
    }
}
