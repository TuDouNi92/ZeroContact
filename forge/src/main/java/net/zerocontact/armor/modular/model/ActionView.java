package net.zerocontact.armor.modular.model;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record ActionView(
        ResourceLocation actionId,
        Component label,
        boolean available,
        Component unavailableReason
) {
}
