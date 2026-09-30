package net.zerocontact.armor.modular.module.radio.model;

import java.util.List;

public record RadioProfile(
        int homeChannel,
        List<Integer> subChannels
) {
}
