package net.zerocontact.armor.modular.module.beacon.service;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.module.beacon.container.BeaconContainer;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class BeaconController implements ModuleController {

    public static final ResourceLocation DISABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "beacon/disable");
    public static final ResourceLocation RED_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "beacon/red");
    public static final ResourceLocation GREEN_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "beacon/green");
    public static final ResourceLocation BLUE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "beacon/blue");

    @Override
    public Optional<ModuleView> inspect(ModuleContext context) {
        return context.module().getCapability(CapabilityRegistries.BEACON)
                .map(
                        cap -> {
                            ResourceLocation nextAction = switch (cap.getMode()) {
                                case OFF -> RED_ACTION;
                                case RED -> GREEN_ACTION;
                                case GREEN -> BLUE_ACTION;
                                case BLUE -> DISABLE_ACTION;
                            };
                            Supplier<ResourceLocation> state =
                                    () -> cap.getMode() != BeaconContainer.Mode.OFF
                                            ? BeaconContainer.STATE_ON
                                            : BeaconContainer.STATE_OFF;
                            return new ModuleView(
                                    state.get(),
                                    List.of(
                                            new ActionView(
                                                    DISABLE_ACTION,
                                                    Component.literal("Beacon Off"),
                                                    true,
                                                    Component.empty()
                                            ),
                                            new ActionView(
                                                    RED_ACTION,
                                                    Component.literal("Red"),
                                                    true,
                                                    Component.empty()
                                            ),
                                            new ActionView(
                                                    GREEN_ACTION,
                                                    Component.literal("Green"),
                                                    true,
                                                    Component.empty()
                                            ),
                                            new ActionView(
                                                    BLUE_ACTION,
                                                    Component.literal("Blue"),
                                                    true,
                                                    Component.empty()
                                            )
                                    ),
                                    Optional.of(nextAction)
                            );
                        }
                );
    }

    @Override
    public ActionResult execute(ModuleContext context, ModuleAction action) {
        return context.module().getCapability(CapabilityRegistries.BEACON)
                .map(cap -> {

                    if (action.id().equals(DISABLE_ACTION)) {
                        if (!cap.getMode().equals(BeaconContainer.Mode.OFF)) {
                            cap.setMode(BeaconContainer.Mode.OFF);
                            return ActionResult.changed();
                        }
                        return ActionResult.unchanged();
                    } else if (action.id().equals(RED_ACTION)) {
                        if (!cap.getMode().equals(BeaconContainer.Mode.RED)) {
                            cap.setMode(BeaconContainer.Mode.RED);
                            return ActionResult.changed();
                        }
                        return ActionResult.unchanged();
                    } else if (action.id().equals(GREEN_ACTION)) {
                        if (!cap.getMode().equals(BeaconContainer.Mode.GREEN)) {
                            cap.setMode(BeaconContainer.Mode.GREEN);
                            return ActionResult.changed();
                        }
                        return ActionResult.unchanged();
                    } else if (action.id().equals(BLUE_ACTION)) {
                        if (!cap.getMode().equals(BeaconContainer.Mode.BLUE)) {
                            cap.setMode(BeaconContainer.Mode.BLUE);
                            return ActionResult.changed();
                        }
                        return ActionResult.unchanged();
                    } else {
                        return ActionResult.rejected(
                                Component.literal("Illegal operation"));
                    }
                })
                .orElse(ActionResult.rejected(
                        Component.literal("No beacon cap")));
    }
}
