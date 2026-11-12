package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.armor.modular.service.ModuleSyncService;

import java.util.Optional;

public record ModuleActionPacket(EquipmentTarget target, ResourceLocation mountId,
                                 ResourceLocation equipmentItem, ResourceLocation moduleItem,
                                 ResourceLocation actionId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ModuleActionPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "module_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleActionPacket> STREAM_CODEC =
            StreamCodec.ofMember(ModuleActionPacket::encode, ModuleActionPacket::decode);

    @Override
    public CustomPacketPayload.Type<ModuleActionPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeResourceLocation(equipmentItem);
        buf.writeResourceLocation(moduleItem);
        buf.writeResourceLocation(actionId);
    }
    public static ModuleActionPacket decode(RegistryFriendlyByteBuf buf) {
        return new ModuleActionPacket(EquipmentTarget.read(buf), buf.readResourceLocation(),
                buf.readResourceLocation(), buf.readResourceLocation(), buf.readResourceLocation());
    }
    public static void handle(ModuleActionPacket packet, IPayloadContext payloadContext) {
        var player = payloadContext.player() instanceof ServerPlayer sender ? sender : null;

        if (player == null || !player.isAlive() || player.isSpectator()) return;
        var equipment = packet.target().resolve(player);
        if (equipment.isEmpty() || !packet.equipmentItem().equals(BuiltInRegistries.ITEM.getKey(equipment.getItem()))) return;
        Optional.ofNullable(equipment.getCapability(ModularCapabilityRegistry.MODULAR_EQUIPMENT)).ifPresent(container -> {
            // Work on a copy so rejected actions cannot mutate the equipped module.
            var module = container.getModule(packet.mountId()).copy();
            if (module.isEmpty() || !packet.moduleItem().equals(BuiltInRegistries.ITEM.getKey(module.getItem()))) return;
            ModuleRegistry.getController(module).ifPresent(controller -> {
                var context = new ModuleContext(player, packet.target(), packet.mountId(), module);
                boolean allowed = controller.inspect(context).stream().flatMap(view -> view.actions().stream())
                        .anyMatch(action -> action.actionId().equals(packet.actionId()) && action.available());
                if (allowed) {
                    var result = controller.execute(context, new ModuleAction(packet.actionId()));
                    if (result.status() == ActionResult.Status.CHANGED) container.setModule(packet.mountId(), module);
                    if (result.status() == ActionResult.Status.REJECTED) player.displayClientMessage(result.message(), true);
                }
                // Refresh stale clients even when the action is already applied or unavailable.
                ModuleSyncService.sync(player, packet.target());
            });
        });
    }
}
