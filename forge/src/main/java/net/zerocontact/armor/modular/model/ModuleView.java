package net.zerocontact.armor.modular.model;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/** primaryActionId selects the action bound to the HUD activation key, if supported. */
public record ModuleView(
        ResourceLocation stateId,
        List<ActionView> actions,
        Optional<ResourceLocation> primaryActionId
) {
    public ModuleView(ResourceLocation stateId, List<ActionView> actions) {
        this(stateId, actions, Optional.empty());
    }
}
