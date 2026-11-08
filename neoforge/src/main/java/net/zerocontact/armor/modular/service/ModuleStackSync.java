package net.zerocontact.armor.modular.service;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/** Transports module capabilities inside vanilla item packets, including creative C2S packets. */
public final class ModuleStackSync {
    private static final String MODULES_TAG = "zerocontact:module_sync";

    private ModuleStackSync() {}

    @Nullable
    public static CompoundTag write(ItemStack stack, @Nullable CompoundTag itemTag) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ModularEquipment)) return itemTag;
        return stack.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).map(container -> {
            SimpleModuleContainer snapshot = new SimpleModuleContainer();
            new ArrayList<>(container.getMountedModules().entrySet()).forEach(e->snapshot.setModule(e.getKey(),e.getValue()));
            // Never add transport state to the source stack or its share tag.
            CompoundTag result = itemTag == null ? new CompoundTag() : itemTag.copy();
            CompoundTag transport = new CompoundTag();
            transport.putBoolean("HasItemTag", itemTag != null);
            transport.put("Container", snapshot.serializeNBT());
            result.put(MODULES_TAG, transport);
            return result;
        }).orElse(itemTag);
    }

    public static void read(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ModularEquipment)) return;
        CompoundTag itemTag = stack.getTag();
        if (itemTag == null || !itemTag.contains(MODULES_TAG, Tag.TAG_COMPOUND)) return;
        CompoundTag transport = itemTag.getCompound(MODULES_TAG);
        CompoundTag cleanTag = itemTag.copy();
        cleanTag.remove(MODULES_TAG);
        stack.setTag(!transport.getBoolean("HasItemTag") && cleanTag.isEmpty() ? null : cleanTag);
        if (!transport.contains("Container", Tag.TAG_COMPOUND)) return;

        SimpleModuleContainer snapshot = new SimpleModuleContainer();
        snapshot.deserializeNBT(transport.getCompound("Container"));
        stack.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).ifPresent(container -> {
            // Empty snapshots also replace the old state; removed modules must stay removed.
            new ArrayList<>(container.getMountedModules().keySet()).forEach(container::removeModule);
            snapshot.getMountedModules().forEach(container::setModule);
        });
    }
}
