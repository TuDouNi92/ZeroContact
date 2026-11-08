package net.zerocontact.armor.modular.module.headset.service;

import net.minecraft.world.entity.player.Player;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.armor.modular.module.headset.item.Headset;
import net.zerocontact.armor.modular.module.headset.model.AudioProfile;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.Optional;

public final class HeadsetService {
    /**
     * Must run on the player's owning game thread; audio code must use ClientHeadsetState.
     */
    public static boolean isActive(Player player) {
        if (player == null) return false;
        return ModuleQuery.streamMounted(player)
                .anyMatch(ref ->
                        ref.stack().getCapability(CapabilityRegistries.HEADSET)
                                .map(HeadsetContainer::isHeadsetOn)
                                .orElse(false)
                );
    }

    public static Optional<AudioProfile> getAudioProfile(Player player) {
        if (player == null) return Optional.empty();
        return ModuleQuery.streamMounted(player)
                .<AudioProfile>mapMulti((ref, out) -> {
                    if (ref.stack().getItem() instanceof Headset headset) {
                        out.accept(headset.getAudioProfile());
                    }
                })
                .findAny();
    }
}
