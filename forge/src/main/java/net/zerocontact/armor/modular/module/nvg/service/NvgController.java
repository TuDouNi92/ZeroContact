package net.zerocontact.armor.modular.module.nvg.service;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.module.nvg.container.NvgContainer;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class NvgController implements ModuleController {

    public static final ResourceLocation ENABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "nvg/enable");
    public static final ResourceLocation DISABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "nvg/disable");

    @Override
    public Optional<ModuleView> inspect(ModuleContext context) {
        return context.module().getCapability(CapabilityRegistries.NVG).map(cap -> {
            NvgPowerService.refresh(context.wearer(), cap);
            Supplier<ResourceLocation> state = () -> {
                if (cap.outOfPower()) return NvgContainer.STATE_NO_POWER;
                if (cap.getEnabled()) {
                    return NvgContainer.STATE_ON;
                }
                return NvgContainer.STATE_OFF;
            };
            return new ModuleView(
                    state.get(),
                    List.of(
                            new ActionView(
                                    ENABLE_ACTION,
                                    Component.literal("开启夜视仪"),
                                    !cap.outOfPower(),
                                    !cap.outOfPower() ? Component.empty() : Component.literal("低电量")
                            ),
                            new ActionView(
                                    DISABLE_ACTION,
                                    Component.literal("关闭夜视仪"),
                                    true,
                                    Component.empty()
                            )
                    ),
                    Optional.of(cap.getEnabled() ? DISABLE_ACTION : ENABLE_ACTION)
            );
        });
    }

    @Override
    public ActionResult execute(ModuleContext context, ModuleAction action) {
        return context.module().getCapability(CapabilityRegistries.NVG).map(nvg -> {
            NvgPowerService.refresh(context.wearer(), nvg);
            if (action.id().equals(ENABLE_ACTION)) {
                if (nvg.outOfPower()) return ActionResult.rejected(Component.literal("Low battery"));
                if (nvg.getEnabled()) return ActionResult.unchanged();
                nvg.setEnabled(true);
                return ActionResult.changed();
            } else if (action.id().equals(DISABLE_ACTION)) {
                if (!nvg.getEnabled()) return ActionResult.unchanged();
                nvg.setEnabled(false);
                return ActionResult.changed();
            }
            return ActionResult.rejected(Component.literal("Illegal operation"));

        }).orElse(ActionResult.rejected(Component.literal("Illegal operation")));
    }
}
