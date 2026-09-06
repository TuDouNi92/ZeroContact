package net.zerocontact.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.zerocontact.network.c2s.*;
import net.zerocontact.network.s2c.AppendSuppressionPacket;
import net.zerocontact.network.s2c.ClientAmmoReloadPacket;
import net.zerocontact.network.s2c.SyncStaminaPacket;
import net.zerocontact.network.s2c.ToggleVisorResultPacket;

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
        net.messageBuilder(SyncStaminaPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncStaminaPacket::new)
                .encoder(SyncStaminaPacket::toBytes)
                .consumerMainThread(SyncStaminaPacket::handle)
                .add();
        net.messageBuilder(ToggleStaminaPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleStaminaPacket::decode)
                .encoder(ToggleStaminaPacket::encode)
                .consumerMainThread(ToggleStaminaPacket::handle)
                .add();
        net.messageBuilder(FlipVisorPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FlipVisorPacket::decode)
                .encoder(FlipVisorPacket::encode)
                .consumerMainThread(FlipVisorPacket::handle)
                .add();
        net.messageBuilder(ToggleVisorResultPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ToggleVisorResultPacket::decode)
                .encoder(ToggleVisorResultPacket::encode)
                .consumerMainThread(ToggleVisorResultPacket::handle)
                .add();
        net.messageBuilder(ToggleBackpackPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleBackpackPacket::decode)
                .encoder(ToggleBackpackPacket::encode)
                .consumerMainThread(ToggleBackpackPacket::handle)
                .add();
        net.messageBuilder(BuyGearsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(BuyGearsPacket::decode)
                .encoder(BuyGearsPacket::encode)
                .consumerMainThread(BuyGearsPacket::handle)
                .add();

        net.messageBuilder(OpenAmmoSelectorPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenAmmoSelectorPacket::decode)
                .encoder(OpenAmmoSelectorPacket::encode)
                .consumerMainThread(OpenAmmoSelectorPacket::handle)
                .add();
        net.messageBuilder(SelectAmmoPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SelectAmmoPacket::decode)
                .encoder(SelectAmmoPacket::encode)
                .consumerMainThread(SelectAmmoPacket::handle)
                .add();
        net.messageBuilder(ClientAmmoReloadPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientAmmoReloadPacket::decode)
                .encoder(ClientAmmoReloadPacket::encode)
                .consumerMainThread(ClientAmmoReloadPacket::handle)
                .add();
        net.messageBuilder(AppendSuppressionPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AppendSuppressionPacket::decode)
                .encoder(AppendSuppressionPacket::encode)
                .consumerMainThread(AppendSuppressionPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
