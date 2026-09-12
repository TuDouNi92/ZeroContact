package net.zerocontact.armor.modular.client.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.armor.modular.service.ModuleSyncService;
import net.zerocontact.capability.CapabilityRegistries;
import java.util.function.Supplier;

public record ModuleActionPacket(EquipmentTarget target, ResourceLocation mountId,
                                 ResourceLocation equipmentItem, ResourceLocation moduleItem,
                                 ResourceLocation actionId) {
    public void encode(FriendlyByteBuf buf) {
        target.write(buf);
        buf.writeResourceLocation(mountId);
        buf.writeResourceLocation(equipmentItem);
        buf.writeResourceLocation(moduleItem);
        buf.writeResourceLocation(actionId);
    }
    public static ModuleActionPacket decode(FriendlyByteBuf buf) {
        return new ModuleActionPacket(EquipmentTarget.read(buf), buf.readResourceLocation(),
                buf.readResourceLocation(), buf.readResourceLocation(), buf.readResourceLocation());
    }
    public static void handle(ModuleActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var player = supplier.get().getSender();
        supplier.get().setPacketHandled(true);
        if (player == null || !player.isAlive() || player.isSpectator()) return;
        var equipment = packet.target().resolve(player);
        if (equipment.isEmpty() || !packet.equipmentItem().equals(ForgeRegistries.ITEMS.getKey(equipment.getItem()))) return;
        equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).ifPresent(container -> {
            // The query returns a copy: commit it only after successful execution.
            var module = container.getModule(packet.mountId());
            if (module.isEmpty() || !packet.moduleItem().equals(ForgeRegistries.ITEMS.getKey(module.getItem()))) return;
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
