package net.zerocontact.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zerocontact.armor.modular.client.network.c2s.*;
import net.zerocontact.network.c2s.*;
import net.zerocontact.network.s2c.AppendSuppressionPacket;
import net.zerocontact.network.s2c.ClientAmmoReloadPacket;
import net.zerocontact.network.s2c.SyncStaminaPacket;
import net.zerocontact.armor.modular.client.network.s2c.SyncModulesPacket;
import net.zerocontact.armor.modular.client.network.s2c.SyncEquipmentCandidatesPacket;
import net.zerocontact.network.s2c.ToggleVisorResultPacket;
import net.zerocontact.armor.modular.client.network.s2c.UpdateNavBoardPacket;

public final class ModMessages {
    private ModMessages() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        // Preserve the old consumerMainThread dispatch for every business handler.
        PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.MAIN);
        registrar.playToClient(SyncStaminaPacket.TYPE, SyncStaminaPacket.STREAM_CODEC, SyncStaminaPacket::handle);
        registrar.playToServer(FlipVisorPacket.TYPE, FlipVisorPacket.STREAM_CODEC, FlipVisorPacket::handle);
        registrar.playToClient(ToggleVisorResultPacket.TYPE, ToggleVisorResultPacket.STREAM_CODEC, ToggleVisorResultPacket::handle);
        registrar.playToServer(ToggleBackpackPacket.TYPE, ToggleBackpackPacket.STREAM_CODEC, ToggleBackpackPacket::handle);
        registrar.playToServer(BuyGearsPacket.TYPE, BuyGearsPacket.STREAM_CODEC, BuyGearsPacket::handle);
        registrar.playToServer(OpenAmmoSelectorPacket.TYPE, OpenAmmoSelectorPacket.STREAM_CODEC, OpenAmmoSelectorPacket::handle);
        registrar.playToServer(SelectAmmoPacket.TYPE, SelectAmmoPacket.STREAM_CODEC, SelectAmmoPacket::handle);
        registrar.playToClient(ClientAmmoReloadPacket.TYPE, ClientAmmoReloadPacket.STREAM_CODEC, ClientAmmoReloadPacket::handle);
        registrar.playToClient(AppendSuppressionPacket.TYPE, AppendSuppressionPacket.STREAM_CODEC, AppendSuppressionPacket::handle);
        registrar.playToClient(SyncModulesPacket.TYPE, SyncModulesPacket.STREAM_CODEC, SyncModulesPacket::handle);
        registrar.playToServer(SelectEquipmentMountPacket.TYPE, SelectEquipmentMountPacket.STREAM_CODEC, SelectEquipmentMountPacket::handle);
        registrar.playToClient(SyncEquipmentCandidatesPacket.TYPE, SyncEquipmentCandidatesPacket.STREAM_CODEC, SyncEquipmentCandidatesPacket::handle);
        registrar.playToServer(OpenModularMenuPacket.TYPE, OpenModularMenuPacket.STREAM_CODEC, OpenModularMenuPacket::handle);
        registrar.playToServer(MountPacket.TYPE, MountPacket.STREAM_CODEC, MountPacket::handle);
        registrar.playToServer(UnMountPacket.TYPE, UnMountPacket.STREAM_CODEC, UnMountPacket::handle);
        registrar.playToServer(ModuleActionPacket.TYPE, ModuleActionPacket.STREAM_CODEC, ModuleActionPacket::handle);
        registrar.playToClient(UpdateNavBoardPacket.TYPE, UpdateNavBoardPacket.STREAM_CODEC, UpdateNavBoardPacket::handle);
        registrar.playToServer(ModuleDataPacket.TYPE, ModuleDataPacket.STREAM_CODEC, ModuleDataPacket::handle);
        registrar.playToServer(EquipPlatePacket.TYPE, EquipPlatePacket.STREAM_CODEC, EquipPlatePacket::handle);
    }

    public static void sendToTrackingAndSelf(CustomPacketPayload msg, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
    }

    public static void sendToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static void sendToPlayer(CustomPacketPayload msg, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, msg);
    }
}
