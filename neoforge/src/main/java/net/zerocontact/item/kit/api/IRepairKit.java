package net.zerocontact.item.kit.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IRepairKit {

    int gerRepairInterval(ItemStack kit);

    boolean canRepair(@NotNull ItemStack kit, ItemStack slotItemStack);

    boolean repair(ServerPlayer player, ItemStack kitStack, ItemStack slotItemStack);

}
