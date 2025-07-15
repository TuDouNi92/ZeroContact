package net.zerocontact.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0f")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE = net;
        net.messageBuilder(NetworkHandler.SyncStaminaPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NetworkHandler.SyncStaminaPacket::new)
                .encoder(NetworkHandler.SyncStaminaPacket::toBytes)
                .consumerMainThread(NetworkHandler.SyncStaminaPacket::handle)
                .add();
        net.messageBuilder(NetworkHandler.ToggleVisorPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(NetworkHandler.ToggleVisorPacket::decode)
                .encoder(NetworkHandler.ToggleVisorPacket::encode)
                .consumerMainThread(NetworkHandler.ToggleVisorPacket::handle)
                .add();
        net.messageBuilder(NetworkHandler.ToggleVisorResultPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NetworkHandler.ToggleVisorResultPacket::decode)
                .encoder(NetworkHandler.ToggleVisorResultPacket::encode)
                .consumerMainThread(NetworkHandler.ToggleVisorResultPacket::handle)
                .add();
        net.messageBuilder(NetworkHandler.ToggleBackpackPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(NetworkHandler.ToggleBackpackPacket::decode)
                .encoder(NetworkHandler.ToggleBackpackPacket::encode)
                .consumerMainThread(NetworkHandler.ToggleBackpackPacket::handle)
                .add();
        net.messageBuilder(NetworkHandler.ToggleBackpackResultPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NetworkHandler.ToggleBackpackResultPacket::decode)
                .encoder(NetworkHandler.ToggleBackpackResultPacket::encode)
                .consumerMainThread(NetworkHandler.ToggleBackpackResultPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
