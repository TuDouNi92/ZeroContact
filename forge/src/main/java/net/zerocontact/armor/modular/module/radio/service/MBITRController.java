package net.zerocontact.armor.modular.module.radio.service;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleDataController;
import net.zerocontact.armor.modular.model.ActionView;
import net.zerocontact.armor.modular.model.ActionResult;
import net.zerocontact.armor.modular.model.ModuleAction;
import net.zerocontact.armor.modular.model.ModuleContext;
import net.zerocontact.armor.modular.model.ModuleView;
import net.zerocontact.armor.modular.module.radio.model.RadioProfile;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MBITRController implements ModuleDataController {
    public static final ResourceLocation STATE_ON = new ResourceLocation(ZeroContact.MOD_ID, "radio/on");
    public static final ResourceLocation STATE_OFF = new ResourceLocation(ZeroContact.MOD_ID, "radio/off");
    public static final ResourceLocation ENABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "radio/enable");
    public static final ResourceLocation DISABLE_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "radio/disable");
    public static final ResourceLocation SET_PROFILE_OPERATION = new ResourceLocation(ZeroContact.MOD_ID, "radio/set_profile");
    public static final int MIN_FREQUENCY = 300;  // 30.0 MHz, stored in tenths
    public static final int MAX_FREQUENCY = 5120; // 512.0 MHz, stored in tenths

    @Override
    public Optional<ModuleView> inspect(ModuleContext context) {
        return context.module().getCapability(CapabilityRegistries.RADIO).map(radio -> {
            boolean activated = radio.getRadioState().radioActivated();
            ResourceLocation actionId = activated ? DISABLE_ACTION : ENABLE_ACTION;
            return new ModuleView(
                    activated ? STATE_ON : STATE_OFF,
                    List.of(new ActionView(
                            actionId,
                            Component.literal(activated ? "关闭电台" : "开启电台"),
                            true,
                            Component.empty()
                    )),
                    Optional.of(actionId),
                    activated ? Optional.of(SET_PROFILE_OPERATION) : Optional.empty()
            );
        });
    }

    @Override
    public ActionResult execute(ModuleContext context, ModuleAction action) {
        if (!ENABLE_ACTION.equals(action.id()) && !DISABLE_ACTION.equals(action.id())) {
            return ActionResult.rejected(Component.literal("Illegal radio operation"));
        }
        return context.module().getCapability(CapabilityRegistries.RADIO).map(radio -> {
            boolean activated = ENABLE_ACTION.equals(action.id());
            if (radio.getRadioState().radioActivated() == activated) {
                return ActionResult.unchanged();
            }
            radio.setRadioActivated(activated);
            return ActionResult.changed();
        }).orElse(ActionResult.rejected(Component.literal("Missing radio")));
    }

    @Override
    public ActionResult executeData(ModuleContext context, ResourceLocation operationId, CompoundTag payload) {
        if (SET_PROFILE_OPERATION.equals(operationId)) {
            return context.module().getCapability(CapabilityRegistries.RADIO).map(radio -> {
                if (!radio.getRadioState().radioActivated()) {
                    return ActionResult.rejected(Component.literal("Radio is off"));
                }
                if (!payload.contains("profile_channel", Tag.TAG_INT)
                        || !payload.contains("sub_channel", Tag.TAG_LIST)) {
                    return ActionResult.rejected(Component.literal("Invalid radio profile"));
                }
                int profileChannel = payload.getInt("profile_channel");
                if (profileChannel < MIN_FREQUENCY || profileChannel > MAX_FREQUENCY) {
                    return ActionResult.rejected(Component.literal("Invalid main channel"));
                }
                List<Integer> subChannels = new ArrayList<>();
                if (!(payload.get("sub_channel") instanceof ListTag listTag)) {
                    return ActionResult.rejected(Component.literal("Invalid group channels"));
                }
                if (!listTag.isEmpty() && listTag.getElementType() != Tag.TAG_INT) {
                    return ActionResult.rejected(Component.literal("Invalid group channels"));
                }
                if (listTag.size() > 16) {
                    return ActionResult.rejected(Component.literal("Too many group channels"));
                }
                for (Tag tag : listTag) {
                    if (!(tag instanceof IntTag number)) {
                        return ActionResult.rejected(Component.literal("Invalid group channel"));
                    }
                    int channel = number.getAsInt();
                    if (channel < MIN_FREQUENCY || channel > MAX_FREQUENCY) {
                        return ActionResult.rejected(Component.literal("Invalid group channel"));
                    }
                    subChannels.add(channel);
                }
                RadioProfile profile = new RadioProfile(profileChannel, List.copyOf(subChannels));
                if (radio.getProfile().equals(profile)) return ActionResult.unchanged();
                radio.setProfile(profile);
                return ActionResult.changed();
            }).orElse(ActionResult.rejected(Component.literal("Missing radio")));
        }
        return ActionResult.rejected(Component.literal("Illegal radio operation"));
    }
}
