package net.zerocontact.armor.modular.module.nvg.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.Tag;
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
            ModuleQuery.streamMounted(serverPlayer).forEach(ref -> ref.stack().getCapability(CapabilityRegistries.NVG).ifPresent(cap -> {
                // Mounted modules do not receive vanilla inventoryTick; initialize old saves here too.
                if (ref.stack().getItem() instanceof GeoItem
                        && (ref.stack().getTag() == null
                        || !ref.stack().getTag().contains(GeoItem.ID_NBT_KEY, Tag.TAG_ANY_NUMERIC))) {
                    GeoItem.getOrAssignId(ref.stack(), serverPlayer.serverLevel());
                    dirty.add(ref.equipmentTarget());
                }
                if (cap.getEnabled()) {
                    cap.tick();
                    if (cap.outOfPower()) {
                        cap.setEnabled(false);
                        dirty.add(ref.equipmentTarget());
                    } else if (serverPlayer.tickCount % 20 == 0) {
                        dirty.add(ref.equipmentTarget());
                    }
                }
            }));
            dirty.forEach(target -> ModuleSyncService.sync(serverPlayer, target));
        }
    }
}
