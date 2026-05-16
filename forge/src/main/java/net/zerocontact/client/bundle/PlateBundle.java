package net.zerocontact.client.bundle;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record PlateBundle(NonNullList<ItemStack> items, int weight,ItemStack armorStack) implements TooltipComponent {
}
