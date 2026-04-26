package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.animation_data.AnimateData;

public interface Toggleable {
    ResourceLocation getVisorTexture();

    boolean getEnabled(ItemStack stack);
    boolean isTriggered(ItemStack stack);
    void setTriggered(boolean toggling,ItemStack stack);

    AnimateData.VisorAnimateData readAnimData(ItemStack stack);
    void saveAnimData(AnimateData.VisorAnimateData animateData, ItemStack stack);
    default void saveStatus(ItemStack stack, boolean switchOn) {

    }

    default Boolean readStatus(ItemStack stack, String key) {
        return stack.getOrCreateTag().getBoolean(key);
    }

    default boolean flipState(ItemStack stack) {
        return false;
    }
    interface Backpack{
        void setToggling(ItemStack stack,boolean isOpen);
        boolean getToggling(ItemStack stack);
    }
}
