package net.zerocontact.armor.modular.module.headset.service;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class HeadsetController implements ModuleController {
    public static final ResourceLocation DISABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "headset/disable");
    public static final ResourceLocation ENABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "headset/enable");

    @Override
    public Optional<ModuleView> inspect(ModuleContext context) {
        return context.module().getCapability(CapabilityRegistries.HEADSET).map(
                cap -> {
                    Supplier<ResourceLocation> state = () -> cap.isHeadsetOn() ? HeadsetContainer.STATE_ON : HeadsetContainer.STATE_OFF;
                    return new ModuleView(
                            state.get(),
                            List.of(
                                    new ActionView(
                                            DISABLE_ACTION,
                                            Component.literal("Headset Off"),
                                            true,
                                            Component.empty()
                                    ),
                                    new ActionView(
                                            ENABLE_ACTION,
                                            Component.literal("Headset on"),
                                            true,
                                            Component.empty()
                                    )
                            ),
                            Optional.of(cap.isHeadsetOn() ? DISABLE_ACTION : ENABLE_ACTION)
                    );
                }
        );
    }

    @Override
    public ActionResult execute(ModuleContext context, ModuleAction action) {
        return context.module().getCapability(CapabilityRegistries.HEADSET).map(
                cap -> {
                    if (action.id().equals(ENABLE_ACTION)) {
                        if (!cap.isHeadsetOn()) {
                            cap.setHeadsetOn(true);
                            return ActionResult.changed();
                        }
                        return ActionResult.unchanged();
                    } else if (action.id().equals(DISABLE_ACTION)) {
                        if (cap.isHeadsetOn()) {
                            cap.setHeadsetOn(false);
                            return ActionResult.changed();
                        }
                        return ActionResult.unchanged();
                    }
                    return ActionResult.rejected(Component.literal("Illegal operation"));
                }
        ).orElse(ActionResult.rejected(Component.literal("Illegal operation")));
    }
}
