package net.zerocontact.armor.modular.module.radio.model;

public record RadioState(
        boolean radioActivated,
        int currentChannel
) {
}
