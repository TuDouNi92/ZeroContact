package net.zerocontact.armor.modular.module.nvg.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.Tag;
import net.zerocontact.armor.modular.module.nvg.service.NvgPowerService;
import software.bernie.geckolib.animatable.GeoItem;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.service.ModuleSyncService;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.capability.CapabilityRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NvgTickHandler {

    @SubscribeEvent
    public static void serverTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var player = event.player;
        if (player instanceof ServerPlayer serverPlayer) {
            Set<EquipmentTarget> dirty = new HashSet<>();
            var modules = ModuleQuery.streamMounted(serverPlayer).toList();
            // Consume first, then refresh every NVG so shared batteries have one final state.
            modules.forEach(ref -> ref.stack().getCapability(CapabilityRegistries.NVG).ifPresent(cap -> {
                if (!cap.getEnabled()) return;
                var supply = NvgPowerService.findSupply(serverPlayer);
                if (supply.isPresent()) {
                    var external = supply.get();
                    external.battery().tick();
                    if (external.battery().outOfPower() || serverPlayer.tickCount % 20 == 0) {
                        dirty.add(external.module().equipmentTarget());
                    }
                } else {
                    cap.consumeInternalBattery();
                }
            }));

            modules.forEach(ref -> ref.stack().getCapability(CapabilityRegistries.NVG).ifPresent(cap -> {
                // Mounted modules do not receive vanilla inventoryTick; initialize old saves here too.
                if (ref.stack().getItem() instanceof GeoItem
                        && (ref.stack().getTag() == null
                        || !ref.stack().getTag().contains(GeoItem.ID_NBT_KEY, Tag.TAG_ANY_NUMERIC))) {
                    GeoItem.getOrAssignId(ref.stack(), serverPlayer.serverLevel());
                    dirty.add(ref.equipmentTarget());
                }
                var previous = cap.getPowerStatus();
                NvgPowerService.refresh(serverPlayer, cap);
                var current = cap.getPowerStatus();
                if (cap.getEnabled() && cap.outOfPower()) {
                    cap.setEnabled(false);
                    dirty.add(ref.equipmentTarget());
                }
                if (previous.source() != current.source() || previous.powered() != current.powered()
                        || previous.capacity() != current.capacity() || serverPlayer.tickCount % 20 == 0) {
                    // Include disabled NVGs: attaching a battery can make Enable available.
                    dirty.add(ref.equipmentTarget());
                }
            }));
            dirty.forEach(target -> ModuleSyncService.sync(serverPlayer, target));
        }
    }
}
