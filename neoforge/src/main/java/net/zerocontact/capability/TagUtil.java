package net.zerocontact.capability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

public class TagUtil {

    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return new CompoundTag();
        return data.copyTag();
    }

    public static void updateTag(ItemStack stack, Consumer<CompoundTag> tagConsumer) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tagConsumer);
    }

}
