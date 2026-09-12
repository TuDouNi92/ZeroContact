package net.zerocontact.armor.modular.model;

import net.minecraft.network.chat.Component;

public record ActionResult(
        Status status,
        Component message
) {
    public enum Status {
        CHANGED,
        UNCHANGED,
        REJECTED
    }

    public static ActionResult changed() {
        return new ActionResult(Status.CHANGED, Component.empty());
    }

    public static ActionResult unchanged() {
        return new ActionResult(Status.UNCHANGED, Component.empty());
    }

    public static ActionResult rejected(Component reason) {
        return new ActionResult(Status.REJECTED, reason);
    }
}
