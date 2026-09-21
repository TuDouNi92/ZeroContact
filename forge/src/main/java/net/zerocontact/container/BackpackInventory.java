package net.zerocontact.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/** One live inventory shared by menu slots and capability consumers. */
public final class BackpackInventory extends ItemStackHandler implements Container {
    private final ItemStack owner;

    public BackpackInventory(ItemStack owner, int size) {
        super(size);
        this.owner = owner;
        CompoundTag data = new CompoundTag();
        data.putInt("Size", size);
        CompoundTag tag = owner.getTag();
        if (tag != null) {
            data.put("Items", tag.getList("inventory", Tag.TAG_COMPOUND).copy());
        }
        deserializeNBT(data);
    }

    @Override
    protected void onContentsChanged(int slot) {
        setChanged();
    }

    @Override
    public void setChanged() {
        owner.getOrCreateTag().put("inventory", serializeNBT().getList("Items", Tag.TAG_COMPOUND));
    }

    @Override
    public int getContainerSize() {
        return getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return extractItem(slot, amount, false);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getStackInSlot(slot);
        setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < getSlots(); slot++) {
            setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
}
