package net.zerocontact.armor.modular.module.pouch.container;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.zerocontact.ZeroContact;
import org.jetbrains.annotations.NotNull;

public class AdminPouchContainer extends ComponentItemHandler {
    public static final ResourceLocation HAS_MAP_SATE = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID,"admin_p/has_map");
    public static final ResourceLocation NO_MAP_SATE = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID,"admin_p/no_map");

    public boolean hasMap() {
        return !getStackInSlot(0).isEmpty();
    }


    public AdminPouchContainer(ItemStack stack) {
        super(stack, DataComponents.CONTAINER, 1);
    }

    public ItemStack insertMap(@NotNull ItemStack mapStack) {
        return insertItem(0, mapStack, false);
    }

    public ItemStack extractMap() {
        return extractItem(0, 1, false);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof MapItem || stack.is(Items.MAP);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

}
