package net.zerocontact.client.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.forge_registries.ModMenus;
import net.zerocontact.item.block.WorkBenchEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkbenchMenu extends AbstractContainerMenu {
    //client
    public WorkbenchMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos());
    }

    //server
    public WorkbenchMenu(int containerId, Inventory playerInv, BlockPos blockPos) {
        super(ModMenus.WORKBENCH_MENU.get(), containerId);
        if (playerInv.player.level().getBlockEntity(blockPos) instanceof WorkBenchEntity workBenchEntity) {
            blockEntity = workBenchEntity;
        }
    }
    public @Nullable WorkBenchEntity blockEntity = null;

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void removed(@NotNull Player player) {
        if (blockEntity != null) {
            blockEntity.triggerAnim("laptop", "close");
        }
        super.removed(player);
    }
}
