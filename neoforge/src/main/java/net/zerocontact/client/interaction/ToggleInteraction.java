package net.zerocontact.client.interaction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ToggleInteraction {
    public static Optional<ItemStack> toggleHelmetVisor(ItemStack oldStack, Item newItem, Class<?> clazz) {
        if (clazz.isAssignableFrom(oldStack.getItem().getClass()) && clazz.isAssignableFrom(newItem.getClass())) {
            if (!oldStack.getItem().getClass().equals(newItem.getClass())) {
                ItemStack newItemstack = new ItemStack(newItem);
                newItemstack.setDamageValue(oldStack.getDamageValue());
                return Optional.of(newItemstack);
            }
        }
        return Optional.empty();
    }
}
