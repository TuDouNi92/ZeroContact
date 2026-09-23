package net.zerocontact.armor.modular.module.pouch.service;

import net.minecraft.sounds.SoundSource;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.armor.modular.model.ActionResult;
import net.zerocontact.armor.modular.model.ModuleAction;
import net.zerocontact.armor.modular.model.ModuleContext;
import net.zerocontact.armor.modular.model.ModuleView;
import net.zerocontact.armor.modular.model.ActionView;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.ZeroContact;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.Optional;
import java.util.List;

public class NavBoardController implements ModuleController {
    public static final ResourceLocation ENABLE = new ResourceLocation(ZeroContact.MOD_ID, "navboard/enable");
    public static final ResourceLocation DISABLE = new ResourceLocation(ZeroContact.MOD_ID, "navboard/disable");

    @Override
    public Optional<ModuleView> inspect(ModuleContext context) {
        return context.module().getCapability(CapabilityRegistries.NAV_BOARD).map(cap -> {
            var action = cap.isEnabled() ? DISABLE : ENABLE;
            return new ModuleView(cap.isEnabled() ? NavBoardContainer.STATE_ON : NavBoardContainer.STATE_OFF,
                    List.of(new ActionView(action, Component.literal(cap.isEnabled() ? "Disable" : "Enable"),
                            true, Component.empty())), Optional.of(action));
        });
    }

    @Override
    public ActionResult execute(ModuleContext context, ModuleAction action) {
        return context.module().getCapability(CapabilityRegistries.NAV_BOARD).map(cap -> {
            if (!action.id().equals(ENABLE) && !action.id().equals(DISABLE)) {
                return ActionResult.rejected(Component.literal("Illegal Operation"));
            }
            boolean enabled = action.id().equals(ENABLE);
            if (cap.isEnabled() == enabled) return ActionResult.unchanged();
            cap.setEnabled(enabled);
            context.wearer().playNotifySound(ModSoundEventsReg.NAV_BOARD, SoundSource.PLAYERS, 0.15f, 1.0f);
            return ActionResult.changed();
        }).orElse(ActionResult.rejected(Component.literal("Missing navigation board")));
    }
}
