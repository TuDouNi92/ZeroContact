package net.zerocontact.armor.modular.module.pouch.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraftforge.items.ItemStackHandler;
import net.zerocontact.ZeroContact;
import org.jetbrains.annotations.NotNull;

public class AdminPouchContainer extends ItemStackHandler {
    public static final ResourceLocation HAS_MAP_SATE = new ResourceLocation(ZeroContact.MOD_ID,"admin_p/has_map");
    public static final ResourceLocation NO_MAP_SATE = new ResourceLocation(ZeroContact.MOD_ID,"admin_p/no_map");

    private static final String NBT_HAS_MAP = "has_map";

    public boolean hasMap() {
        return !getStackInSlot(0).isEmpty();
    }


    public AdminPouchContainer() {
        super(1);
    }

    public ItemStack insertMap(@NotNull ItemStack mapStack) {
        return insertItem(0, mapStack, false);
    }

    public ItemStack extractMap() {
        return extractItem(0, 1, false);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.getItem() instanceof MapItem || stack.is(Items.MAP);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putBoolean(NBT_HAS_MAP, hasMap());
        return nbt;
    }
}
