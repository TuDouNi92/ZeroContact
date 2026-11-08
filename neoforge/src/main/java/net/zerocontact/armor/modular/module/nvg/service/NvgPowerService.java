package net.zerocontact.armor.modular.module.nvg.service;

import net.minecraft.world.entity.player.Player;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.battery.container.BatteryContainer;
import net.zerocontact.armor.modular.module.nvg.container.NvgContainer;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.Comparator;
import java.util.Optional;

/** Server-side supply selection shared by ticking and action validation. */
public final class NvgPowerService {
    private NvgPowerService() {}

    public record Supply(ModuleQuery.MountedModuleRef module, BatteryContainer battery) {}

    public static Optional<Supply> findSupply(Player wearer) {
        return ModuleQuery.streamMounted(wearer)
                .sorted(Comparator.comparing((ModuleQuery.MountedModuleRef ref) -> ref.equipmentTarget().slot())
                        .thenComparingInt(ref -> ref.equipmentTarget().index())
                        .thenComparing(ref -> ref.mountId().toString()))
                .flatMap(ref -> ref.stack().getCapability(CapabilityRegistries.BATTERY)
                        .map(battery -> new Supply(ref, battery)).stream())
                .filter(supply -> !supply.battery().outOfPower())
                .findFirst();
    }

    public static void refresh(Player wearer, NvgContainer nvg) {
        if (wearer.level().isClientSide()) return;
        var supply = findSupply(wearer);
        nvg.refreshPowerStatus(supply.map(s -> (float) s.battery().getBattery()).orElse(0F),
                supply.map(s -> (float) s.battery().getMaxBattery()).orElse(0F));
    }
}
