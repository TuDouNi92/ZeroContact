package net.zerocontact.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IndexSimpleContainer extends SimpleContainer {
    public IndexSimpleContainer(int size) {
        super(size);
    }

    public @NotNull ListTag createIndexTag() {
        ListTag listTag = new ListTag();

        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Slot",(short) i);
                listTag.add(itemStack.save(tag));
            }
        }

        return listTag;
    }

    public void fromIndexTag(@NotNull ListTag containerNbt) {
        this.clearContent();
        for(int i = 0; i < containerNbt.size(); ++i) {
            CompoundTag compoundTag = containerNbt.getCompound(i);
            int slot = compoundTag.getInt("Slot");
            ItemStack itemStack = ItemStack.of(compoundTag);
            if (!itemStack.isEmpty()) {
                this.setItem(slot,itemStack);
            }
        }
    }
}
