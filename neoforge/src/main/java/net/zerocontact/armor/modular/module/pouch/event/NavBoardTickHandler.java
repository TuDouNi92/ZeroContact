package net.zerocontact.armor.modular.module.pouch.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.client.network.s2c.UpdateNavBoardPacket;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.service.ModuleSyncService;
import net.zerocontact.network.ModMessages;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = ZeroContact.MOD_ID)
public class NavBoardTickHandler {
    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return;
        var modules = ModuleQuery.streamMounted(event.getEntity()).toList();
        // Mounted modules do not receive inventoryTick. Give each one a persistent animation identity.
        Set<EquipmentTarget> initialized = new HashSet<>();
        modules.forEach(ref -> {
            var stack = ref.stack();
            if (stack.getItem() instanceof GeoItem && Optional.ofNullable(stack.getCapability(ModularCapabilityRegistry.NAV_BOARD)).isPresent()
                    && (GeoItem.getId(stack) == Long.MAX_VALUE)) {
                GeoItem.getOrAssignId(stack, player.serverLevel());
                initialized.add(ref.equipmentTarget());
            }
        });
        // Send the initialized stacks before sampling notifications so the client resolves the same instance.
        initialized.forEach(target -> ModuleSyncService.sync(player, target));
        modules.forEach(
                ref -> Optional.ofNullable(ref.stack().
                        getCapability(ModularCapabilityRegistry.NAV_BOARD))
                        .ifPresent(cap -> {
                            if (cap.tick()) {
                                ModMessages.sendToPlayer(new UpdateNavBoardPacket(
                                        ref.equipmentTarget(), ref.mountId(),
                                        BuiltInRegistries.ITEM.getKey(ref.equipmentTarget().resolve(player).getItem()),
                                        BuiltInRegistries.ITEM.getKey(ref.stack().getItem()),
                                        player.level().dimension().location()), player);
                            }
                        })
        );
    }
}
