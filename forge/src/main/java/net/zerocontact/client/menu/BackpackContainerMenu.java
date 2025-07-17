package net.zerocontact.client.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.container.IndexSimpleContainer;
import net.zerocontact.forge_registries.ModMenus;
import net.zerocontact.item.backpack.BaseBackpack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class BackpackContainerMenu extends AbstractContainerMenu {
    private final IndexSimpleContainer container;
    private final TriggerSource triggerSource;
    public ItemStack renderStack;

    public enum TriggerSource {
        USE,
        KEY
    }

    public BackpackContainerMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readInt(), buf.readEnum(TriggerSource.class));
    }

    public BackpackContainerMenu(int containerId, Inventory playerInv, int size, TriggerSource source) {
        super(ModMenus.BACKPACK_CONTAINER.get(), containerId);
        this.container = new IndexSimpleContainer(size);
        this.triggerSource = source;
        ItemStack mainHandStack = playerInv.player.getMainHandItem();
        if (triggerSource == BackpackContainerMenu.TriggerSource.USE) {
            renderStack = mainHandStack;
            readInvfromTag(mainHandStack);
        } else if (source == BackpackContainerMenu.TriggerSource.KEY) {
            CuriosApi.getCuriosInventory(playerInv.player).ifPresent(inventoryHandler -> inventoryHandler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                renderStack = backpackStack;
                readInvfromTag(backpackStack);
            }));
        }

        CustomInventory customInventory = new CustomInventory(this, size);
        new PlayerInventory(playerInv, customInventory);
    }

    private void readInvfromTag(ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BaseBackpack) {
            Optional<CompoundTag> inventoryTag = Optional.ofNullable(backpackStack.getTag());
            inventoryTag.ifPresent(inventoryTag1 -> {
                ListTag listTag = inventoryTag1.getList("inventory", Tag.TAG_COMPOUND);
                container.fromIndexTag(listTag);
            });
        }
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
        protected BackpackContainerMenu menu;

        CustomInventory(BackpackContainerMenu menu, int size) {
            this.menu = menu;
            addCustomInventory(size);
        }

        private void addCustomInventory(int size) {
            int cols = Mth.ceil(Mth.sqrt(size));
            int rows = Mth.ceil((double) size / cols);
            int startX = (176 - cols * 18) / 2;
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    customInvY = 16 + i * 18;
                    menu.addSlot(new Slot(container, j + i * cols, startX + j * 18, customInvY));
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
                    playerInvY = customInventory.customInvY + 24 + i * 18;
                    customInventory.menu.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, playerInvY)
                    );

                }
            }
            for (int j = 0; j < 9; ++j) {
                customInventory.menu.addSlot(new Slot(inventory, j, 8 + j * 18, playerInvY + 24));
            }
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        if (triggerSource == BackpackContainerMenu.TriggerSource.USE
                && player instanceof ServerPlayer serverPlayer
        ) {
            writeInvToTag(serverPlayer.getMainHandItem());
        } else if (triggerSource == TriggerSource.KEY) {
            CuriosApi.getCuriosInventory(player).ifPresent(inventoryHandler -> inventoryHandler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                writeInvToTag(backpackStack);
            }));
        }
        super.removed(player);
    }

    private void writeInvToTag(ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BaseBackpack) {
            backpackStack.getOrCreateTag().put("inventory", container.createIndexTag());
        }
    }
}
