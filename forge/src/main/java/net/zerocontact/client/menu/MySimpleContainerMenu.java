package net.zerocontact.client.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.forge_registries.ModMenus;
import org.jetbrains.annotations.NotNull;

public class MySimpleContainerMenu extends AbstractContainerMenu {
    private final SimpleContainer container;
    private final int size;

    public MySimpleContainerMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readInt());
    }

    public MySimpleContainerMenu(int containerId, Inventory playerInv, int size) {
        super(ModMenus.BACKPACK_CONTAINER.get(), containerId);
        this.size = size;
        this.container = new SimpleContainer(size);
        CustomInventory customInventory = new CustomInventory(this, size);
        new PlayerInventory(playerInv, customInventory);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    class CustomInventory {
        protected int customInvY;
        protected MySimpleContainerMenu menu;

        CustomInventory(MySimpleContainerMenu menu, int size) {
            this.menu = menu;
            addCustomInventory(size);
        }

        private void addCustomInventory(int size) {
            int cols = Mth.ceil(Mth.sqrt(size));
            int rows = Mth.ceil((double) size / cols);
            int startX =(176-cols*18)/2;
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    customInvY = 16+i * 18;
                    menu.addSlot(new Slot(container, j + i * cols, startX+j*18, customInvY)
                            .setBackground(
                                    new ResourceLocation("minecraft", "textures/atlas/blocks.png"),
                                    new ResourceLocation("minecraft", "item/empty_slot_sword.png")));
                }
            }
        }

    }

    class PlayerInventory {
        private int playerInvY;

        PlayerInventory(Inventory playerInv, CustomInventory customInventory) {
            addPlayerInventory(playerInv, customInventory);
        }

        private void addPlayerInventory(Inventory inventory, CustomInventory customInventory) {
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    playerInvY = customInventory.customInvY+24 + i * 18;
                    customInventory.menu.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, playerInvY)
                            .setBackground(
                                    new ResourceLocation("minecraft", "textures/atlas/blocks.png"),
                                    new ResourceLocation("minecraft", "item/empty_slot_sword.png"))
                    );

                }
            }
            for (int j = 0; j < 9; ++j) {
                customInventory.menu.addSlot(new Slot(inventory, j, 8 + j * 18, playerInvY + 24));
            }
        }
    }

}
