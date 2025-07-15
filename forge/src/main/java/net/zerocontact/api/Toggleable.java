package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Toggleable {
    ResourceLocation getVisorTexture();

    boolean getEnabled();

    void setToggling(boolean toggling);

    default void saveData(ItemStack stack, boolean switchOn) {
        stack.getOrCreateTag().putBoolean("VisorOn", switchOn);
    }

    default Boolean readData(ItemStack stack, String key) {
        return stack.getOrCreateTag().getBoolean(key);
    }

    default boolean switchVisorState(Player player) {
        AtomicBoolean state = new AtomicBoolean(false);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() == this) {
            state.set(readData(helmet, "VisorOn"));
            saveData(helmet, !state.get());
        }
        return !state.get();
    }
    interface Backpack{
        void setToggling(boolean open);
        boolean getToggling();
    }
}
