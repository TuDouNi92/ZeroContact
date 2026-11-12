package net.zerocontact.item.kit;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RepairSession {

    private int elapsedTicks;
    private final int targetSlot;
    private final @Nullable AbstractContainerMenu menu;
    private final ItemStack snapStack;

    public RepairSession(int targetSlot, @Nullable AbstractContainerMenu menu) {
        this.targetSlot = targetSlot;
        this.menu = menu;
        this.snapStack = menu != null ? menu.slots.get(targetSlot).getItem(): ItemStack.EMPTY;
    }

    public void tick() {
        elapsedTicks++;
    }

    public boolean shouldRepairAtTick(int interval) {
        return elapsedTicks % interval == 0;
    }


    public int getTargetSlot() {
        return targetSlot;
    }

    public @Nullable AbstractContainerMenu getMenu() {
        return menu;
    }

    public ItemStack getSnapStack() {
        return snapStack;
    }
}
