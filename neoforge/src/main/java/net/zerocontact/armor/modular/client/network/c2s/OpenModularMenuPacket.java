package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;

public record OpenModularMenuPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenModularMenuPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "open_modular_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenModularMenuPacket> STREAM_CODEC =
            StreamCodec.ofMember(OpenModularMenuPacket::encode, OpenModularMenuPacket::decode);

    @Override
    public CustomPacketPayload.Type<OpenModularMenuPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf __) {
    }

    public static OpenModularMenuPacket decode(RegistryFriendlyByteBuf __) {
        return new OpenModularMenuPacket();
    }

    public static void handle(OpenModularMenuPacket __, IPayloadContext context) {
        // Registered with HandlerThread.MAIN.
        var player = context.player() instanceof ServerPlayer sender ? sender : null;
        if (player != null) {
            // Use the shared opener: the client constructor requires its extra data payload.
            EquipmentMenu.open(player, Component.translatable("screen.zerocontact.equipment.title"));
        }
    }
}
