package net.zerocontact.armor.modular.module.radio.service;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.radio.api.MBITR;
import net.zerocontact.armor.modular.module.radio.model.RadioProfile;
import net.zerocontact.armor.modular.module.radio.model.RadioState;
import net.zerocontact.armor.modular.module.radio.model.RadioTransmission;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.Optional;

public class MBITRService {

    public static Optional<RadioState> getRadioState(ItemStack radio) {
        return radio.getCapability(CapabilityRegistries.RADIO).map(MBITR::getRadioState);
    }

    public static void setProfile(ItemStack radio, RadioProfile radioProfile) {
        radio.getCapability(CapabilityRegistries.RADIO).ifPresent(r -> r.setProfile(radioProfile));
    }


    public static boolean canReceiveFromSender(ServerPlayer receiver, RadioTransmission source) {
        Optional<MBITR> receiverRadio = findActiveRadio(receiver);
        if (receiverRadio.isEmpty()) return false;
        return receiverRadio.map(mbitr -> mbitr.canReceiveFrom(source)).orElse(false);
    }

    public static Optional<MBITR> findActiveRadio(Player player) {
        return ModuleQuery.streamMounted(player)
                .map(ref -> ref.stack().getCapability(CapabilityRegistries.RADIO).map(r -> r))
                .flatMap(Optional::stream)
                .filter(r->r.getRadioState().radioActivated())
                .findFirst();
    }

}
