package net.zerocontact.armor.modular.module.pouch.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.client.network.s2c.UpdateNavBoardPacket;
import net.zerocontact.network.ModMessages;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.capability.CapabilityRegistries;
import net.minecraft.nbt.Tag;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.service.ModuleSyncService;
import software.bernie.geckolib.animatable.GeoItem;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NavBoardTickHandler {
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return;
        var modules = ModuleQuery.streamMounted(event.player).toList();
        // Mounted modules do not receive inventoryTick. Give each one a persistent animation identity.
        Set<EquipmentTarget> initialized = new HashSet<>();
        modules.forEach(ref -> {
            var stack = ref.stack();
            if (stack.getItem() instanceof GeoItem && stack.getCapability(CapabilityRegistries.NAV_BOARD).isPresent()
                    && (stack.getTag() == null || !stack.getTag().contains(GeoItem.ID_NBT_KEY, Tag.TAG_ANY_NUMERIC))) {
                GeoItem.getOrAssignId(stack, player.serverLevel());
                initialized.add(ref.equipmentTarget());
            }
        });
        // Send the initialized stacks before sampling notifications so the client resolves the same instance.
        initialized.forEach(target -> ModuleSyncService.sync(player, target));
        modules.forEach(
                ref -> ref.stack().
                        getCapability(CapabilityRegistries.NAV_BOARD)
                        .ifPresent(cap -> {
                            if (cap.tick()) {
                                ModMessages.sendToPlayer(new UpdateNavBoardPacket(
                                        ref.equipmentTarget(), ref.mountId(),
                                        ForgeRegistries.ITEMS.getKey(ref.equipmentTarget().resolve(player).getItem()),
                                        ForgeRegistries.ITEMS.getKey(ref.stack().getItem()),
                                        player.level().dimension().location()), player);
                            }
                        })
        );
    }
}
