package net.zerocontact.armor.modular.module.headset.service;

import net.minecraft.world.entity.player.Player;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.capability.CapabilityRegistries;

public final class HeadsetManager {
    /** Must run on the player's owning game thread; audio code must use ClientHeadsetState. */
    public static boolean isActive(Player player) {
        if (player == null) return false;
        return ModuleQuery.streamMounted(player)
                .anyMatch(ref ->
                        ref.stack().getCapability(CapabilityRegistries.HEADSET)
                                .map(HeadsetContainer::isHeadsetOn)
                                .orElse(false)
                );
    }
}
