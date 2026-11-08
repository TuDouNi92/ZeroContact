package net.zerocontact.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IRepairKit {

    int getKitRepairTime();

    void setKitRepairTime(int ticks);

    boolean shouldRepairAtTick();


    boolean interrupted();

    void setInterrupt(boolean interrupt);

    boolean started();

    boolean finished();

    void setStarted(boolean started);

    int getTargetSlot();

    void setTargetSlot(int slot);

    void setTargetSlotFromInv(ServerPlayer player,ItemStack kitStack);

    void setRepairItem(ServerPlayer player, ItemStack stack);

    boolean canRepair(@NotNull ItemStack kit, ItemStack slotItemStack);

    void repair(ServerPlayer player, ItemStack kitStack, ItemStack slotItemStack);

    void stopRepair();
}
