package net.zerocontact.armor.modular.module.radio.model;

import net.minecraft.world.phys.Vec3;

public record RadioTransmission(
        Vec3 senderPos,
        RadioState radioState
) {
}
