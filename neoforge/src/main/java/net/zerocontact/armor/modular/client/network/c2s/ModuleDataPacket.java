package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.api.armor.modular.ModuleDataController;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.model.ActionResult;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.model.ModuleContext;
import net.zerocontact.armor.modular.model.ModuleView;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.armor.modular.service.ModuleSyncService;

import java.util.Optional;

public record ModuleDataPacket(
        EquipmentTarget target,
        ResourceLocation mountId,
        ResourceLocation equipmentItem,
        ResourceLocation moduleItem,
        ResourceLocation operationId,
        CompoundTag payload
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ModuleDataPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "module_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleDataPacket> STREAM_CODEC =
            StreamCodec.ofMember(ModuleDataPacket::encode, ModuleDataPacket::decode);

    @Override
    public CustomPacketPayload.Type<ModuleDataPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeResourceLocation(equipmentItem);
        buf.writeResourceLocation(moduleItem);
        buf.writeResourceLocation(operationId);
        buf.writeNbt(payload);
    }

    public static ModuleDataPacket decode(RegistryFriendlyByteBuf buf) {
        return new ModuleDataPacket(
                EquipmentTarget.read(buf),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readNbt()
        );
    }

    public static void handle(ModuleDataPacket packet, IPayloadContext payloadContext) {
        var player = payloadContext.player() instanceof ServerPlayer sender ? sender : null;

        if (player == null || !player.isAlive() || player.isSpectator()) return;
        if (packet.payload() == null) return;
        var equipment = packet.target().resolve(player);
        if (equipment.isEmpty() || !packet.equipmentItem().equals(BuiltInRegistries.ITEM.getKey(equipment.getItem())))
            return;
        Optional.ofNullable(equipment.getCapability(ModularCapabilityRegistry.MODULAR_EQUIPMENT)).ifPresent(container -> {
            // Work on a copy so rejected actions cannot mutate the equipped module.
            var module = container.getModule(packet.mountId()).copy();
            if (module.isEmpty() || !packet.moduleItem().equals(BuiltInRegistries.ITEM.getKey(module.getItem()))) return;
            ModuleRegistry.getController(module).ifPresent(controller -> {
                if (!(controller instanceof ModuleDataController dataController)) return;
                var context = new ModuleContext(player, packet.target(), packet.mountId(), module);
                boolean allowed = controller.inspect(context)
                        .flatMap(ModuleView::operationId)
                        .filter(packet.operationId()::equals)
                        .isPresent();
                if (allowed) {
                    var result = dataController.executeData(context, packet.operationId(), packet.payload());
                    if (result.status() == ActionResult.Status.CHANGED) {
                        container.setModule(packet.mountId(), module);
                    }
                    if (result.status() == ActionResult.Status.REJECTED)
                        player.displayClientMessage(result.message(), true);
                }
                // Refresh stale clients even when the action is already applied or unavailable.
                ModuleSyncService.sync(player, packet.target());
            });
        });
    }
}
