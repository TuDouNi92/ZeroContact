package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.armor.modular.ModuleDataController;
import net.zerocontact.armor.modular.model.ActionResult;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.model.ModuleContext;
import net.zerocontact.armor.modular.model.ModuleView;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.armor.modular.service.ModuleSyncService;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.function.Supplier;

public record ModuleDataPacket(
        EquipmentTarget target,
        ResourceLocation mountId,
        ResourceLocation equipmentItem,
        ResourceLocation moduleItem,
        ResourceLocation operationId,
        CompoundTag payload
) {
    public void encode(FriendlyByteBuf buf) {
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeResourceLocation(equipmentItem);
        buf.writeResourceLocation(moduleItem);
        buf.writeResourceLocation(operationId);
        buf.writeNbt(payload);
    }

    public static ModuleDataPacket decode(FriendlyByteBuf buf) {
        return new ModuleDataPacket(
                EquipmentTarget.read(buf),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readNbt()
        );
    }

    public static void handle(ModuleDataPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var player = supplier.get().getSender();
        supplier.get().setPacketHandled(true);
        if (player == null || !player.isAlive() || player.isSpectator()) return;
        if (packet.payload() == null) return;
        var equipment = packet.target().resolve(player);
        if (equipment.isEmpty() || !packet.equipmentItem().equals(ForgeRegistries.ITEMS.getKey(equipment.getItem())))
            return;
        equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).ifPresent(container -> {
            // The query returns a copy: commit it only after successful execution.
            var module = container.getModule(packet.mountId());
            if (module.isEmpty() || !packet.moduleItem().equals(ForgeRegistries.ITEMS.getKey(module.getItem()))) return;
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
