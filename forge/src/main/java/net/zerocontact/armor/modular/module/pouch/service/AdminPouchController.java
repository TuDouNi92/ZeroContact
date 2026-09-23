package net.zerocontact.armor.modular.module.pouch.service;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.module.pouch.container.AdminPouchContainer;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AdminPouchController implements ModuleController {
    public static final ResourceLocation TAKE_MAP_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "admin_p/take_map");
    public static final ResourceLocation PUT_MAP_ACTION = new ResourceLocation(ZeroContact.MOD_ID, "admin_p/put_map");

    @Override
    public Optional<ModuleView> inspect(ModuleContext context) {
        return context.module().getCapability(CapabilityRegistries.ADMIN_POUCH).map(cap -> {
            Supplier<ResourceLocation> state = () -> {
                if (cap.hasMap()) {
                    return AdminPouchContainer.HAS_MAP_SATE;
                } else {
                    return AdminPouchContainer.NO_MAP_SATE;
                }
            };
            return new ModuleView(
                    state.get(),
                    List.of(
                            new ActionView(
                                    TAKE_MAP_ACTION,
                                    Component.literal("Take out map"),
                                    cap.hasMap(),
                                    Component.literal("No map")
                            ),
                            new ActionView(
                                    PUT_MAP_ACTION,
                                    Component.literal("Put map in"),
                                    !cap.hasMap(),
                                    Component.literal("Already existed")
                            )
                    ),
                    Optional.of(cap.hasMap() ? TAKE_MAP_ACTION : PUT_MAP_ACTION)
            );
        });
    }

    @Override
    public ActionResult execute(ModuleContext context, ModuleAction action) {
        return context.module().getCapability(CapabilityRegistries.ADMIN_POUCH).map(cap -> {
            if (action.id().equals(TAKE_MAP_ACTION)) {
                if (!cap.hasMap()) return ActionResult.unchanged();
                ItemStack extracted = cap.extractMap();
                if (extracted.isEmpty()) {
                    return ActionResult.rejected(Component.literal("Empty stack"));
                }
                if (!context.wearer().getInventory().add(extracted)) {
                    context.wearer().drop(extracted, false);
                }
                context.wearer().playNotifySound(ModSoundEventsReg.POUCH_MAP, SoundSource.PLAYERS,1.0f,1.0f);
                return ActionResult.changed();
            } else if (action.id().equals(PUT_MAP_ACTION)) {
                if (cap.hasMap()) return ActionResult.unchanged();

                ItemStack mainHandItem = context.wearer().getMainHandItem();
                ItemStack offHandItem = context.wearer().getOffhandItem();

                ItemStack heldMap = cap.isItemValid(0, mainHandItem) ? mainHandItem : offHandItem;
                if (!cap.isItemValid(0, heldMap)) {
                    return ActionResult.rejected(Component.literal("Not holding the map"));
                }

                ItemStack remain = cap.insertMap(heldMap.copyWithCount(1));
                if (!remain.isEmpty()) {
                    return ActionResult.rejected(Component.literal("Cannot insert map"));
                }
                heldMap.shrink(1);
                context.wearer().playNotifySound(ModSoundEventsReg.POUCH_MAP, SoundSource.PLAYERS,1.0f,1.0f);
                return ActionResult.changed();
            }
            return ActionResult.rejected(Component.literal("Illegal Operation"));
        }).orElse(ActionResult.rejected(Component.literal("Illegal Operation")));
    }
}
