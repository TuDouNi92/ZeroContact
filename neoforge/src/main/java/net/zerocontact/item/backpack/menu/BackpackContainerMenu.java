package net.zerocontact.item.backpack.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.forge_registries.MenuRegistry;
import net.zerocontact.item.backpack.BaseBackpack;
import net.zerocontact.item.rigs.BaseRigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class BackpackContainerMenu extends AbstractContainerMenu {
    public static final int EQUIPMENT_PREVIEW_SIZE = 24;
    private static final int EQUIPMENT_PREVIEW_PADDING = 8;

    private IItemHandler backpackContainer = new ItemStackHandler(0);
    private IItemHandler rigsContainer = new ItemStackHandler(0);
    private final TriggerSource triggerSource;
    private int openedInventorySlot = -1;
    private ItemStack openedStack = ItemStack.EMPTY;
    public int minSlotX = Integer.MAX_VALUE;
    public int maxSlotX = Integer.MIN_VALUE;
    public int minSlotY = Integer.MAX_VALUE;
    public int maxSlotY = Integer.MIN_VALUE;
    public int guiWidth, guiHeight, rigStackStart;
    private int backpackSlotEnd, rigsSlotEnd;
    public @Nullable ItemStack backpackRenderStack;
    public ItemStack rigsRenderStack;
    public ItemStack allyStack;

    public enum TriggerSource {
        USE,
        KEY,
        ALLY
    }

    //client side
    public BackpackContainerMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readEnum(TriggerSource.class), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    //server side
    public BackpackContainerMenu(int containerId, Inventory playerInv, TriggerSource source, @Nullable ItemStack allyStack) {
        super(MenuRegistry.BACKPACK_CONTAINER.get(), containerId);
        this.triggerSource = source;
        if (triggerSource == TriggerSource.USE) {
            backpackRenderStack = getHandStack(playerInv.player);
            openedStack = backpackRenderStack;
            openedInventorySlot = openedStack == playerInv.player.getMainHandItem()
                    ? playerInv.selected : Inventory.SLOT_OFFHAND;
            bindInventory(backpackRenderStack);
        } else if (source == TriggerSource.KEY) {
            CuriosApi.getCuriosInventory(playerInv.player).ifPresent(inventoryHandler -> {
                inventoryHandler.getStacksHandler(CuriosConstants.BACKPACK).ifPresent(stacksHandler -> {
                    ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                    if (backpackStack.getItem() instanceof BaseBackpack) {
                        backpackRenderStack = backpackStack;
                        bindInventory(backpackStack);
                    }
                });
                inventoryHandler.getStacksHandler(CuriosConstants.RIGS).ifPresent(stacksHandler -> {
                    ItemStack rigsStack = stacksHandler.getStacks().getStackInSlot(0);
                    if (rigsStack.getItem() instanceof BaseRigs) {
                        rigsRenderStack = rigsStack;
                        bindInventory(rigsStack);
                    }
                });
            });

        } else if (source == TriggerSource.ALLY) {
            if (allyStack != null && allyStack.getItem() instanceof BaseBackpack) {
                this.allyStack = allyStack;
                backpackRenderStack = allyStack;
                bindInventory(allyStack);
            }
        }

        CustomInventory customInventory = new CustomInventory(this, backpackContainer.getSlots(), rigsContainer.getSlots());
        new PlayerInventory(playerInv, customInventory);
        int padding = 8;
        this.guiWidth = Math.max(176, maxSlotX + padding);
        this.guiHeight = maxSlotY + padding;
    }

    private ItemStack getHandStack(Player player) {
        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);
        if (mainHandStack.getItem() instanceof BaseBackpack) return mainHandStack;
        return offHandStack.getItem() instanceof BaseBackpack ? offHandStack : ItemStack.EMPTY;
    }

    private void bindInventory(ItemStack stack) {
        Optional.ofNullable(stack.getCapability(Capabilities.ItemHandler.ITEM)).ifPresent(handler -> {
            if (stack.getItem() instanceof BaseRigs) {
                rigsContainer = handler;
            } else if (stack.getItem() instanceof BaseBackpack) {
                backpackContainer = handler;
            }
        });
    }

    @Override
    protected @NotNull Slot addSlot(@NotNull Slot slot) {
        minSlotX = Math.min(minSlotX, slot.x);
        maxSlotX = Math.max(maxSlotX, slot.x + 16);
        minSlotY = Math.min(minSlotY, slot.y);
        maxSlotY = Math.max(maxSlotY, slot.y + 16);
        return super.addSlot(slot);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        ItemStack copy = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) return copy;
        ItemStack sourceStack = sourceSlot.getItem();
        copy = sourceStack.copy();
        if (rigsSlotEnd == 0) rigsSlotEnd = backpackSlotEnd;
        if (index < backpackSlotEnd) {
            if (!this.moveItemStackTo(sourceStack, rigsSlotEnd, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < rigsSlotEnd) {
            if (!this.moveItemStackTo(sourceStack, rigsSlotEnd, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(sourceStack, 0, rigsSlotEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        return copy;
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (!stillValid(player)) return;
        // SWAP accesses the hotbar/offhand directly, bypassing the source slot's mayPickup.
        if (clickType == ClickType.SWAP && button == openedInventorySlot) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // Client slot synchronization replaces ItemStack instances; check identity only on the server.
        return triggerSource != TriggerSource.USE || player.level().isClientSide
                || (!openedStack.isEmpty() && player.getInventory().getItem(openedInventorySlot) == openedStack);
    }

    private Slot playerSlot(Inventory inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                return getContainerSlot() != openedInventorySlot && super.mayPickup(player);
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                // Keep the slot locked by index even when client synchronization replaces its stack.
                return getContainerSlot() != openedInventorySlot && super.mayPlace(stack);
            }
        };
    }

    class CustomInventory {
        protected int backpackCustomInvY;
        protected int rigsCustomInvY;
        protected BackpackContainerMenu menu;

        CustomInventory(BackpackContainerMenu menu, int backpackSize, int rigsSize) {
            this.menu = menu;
            addCustomInventory(backpackSize, rigsSize);
        }

        private void addCustomInventory(int backpackSize, int rigsSize) {
            //Backpack part
            if (backpackSize != 0) {
                int cols = Mth.ceil(Mth.sqrt(backpackSize));
                int rows = Mth.ceil((double) backpackSize / cols);
                int backpackWidth = cols * 18;
                int baseWidth = Math.max(176, backpackWidth + 50);
                int startX = (baseWidth - backpackWidth) / 2;
                for (int i = 0; i < rows; ++i) {
                    for (int j = 0; j < cols; ++j) {
                        backpackCustomInvY = 16 + i * 18;
                        if (j + i * cols >= backpackSize) continue;
                        // ComponentItemHandler returns copies; write mutated stacks back through the slot.
                        menu.addSlot(new ItemHandlerCopySlot(backpackContainer, j + i * cols, startX + j * 18, backpackCustomInvY) {
                            @Override
                            public boolean mayPlace(@NotNull ItemStack stack) {
                                return !(stack.getItem() instanceof BaseBackpack) && super.mayPlace(stack);
                            }
                        });
                    }
                }
                backpackSlotEnd = slots.size();
            }
            //Rigs part
            if (rigsSize != 0) {
                int rCols = Mth.ceil(Mth.sqrt(rigsSize));
                int rRows = Mth.ceil((double) rigsSize / rCols);
                int rigsWidth = rCols * 18;
                int previewSpace = EQUIPMENT_PREVIEW_SIZE + 2 * EQUIPMENT_PREVIEW_PADDING;
                int sectionLeft;
                if (backpackSlotEnd > 0) {
                    sectionLeft = maxSlotX;
                } else {
                    // Center the preview and its slots together when no backpack is equipped.
                    int sectionWidth = previewSpace + rigsWidth;
                    int baseWidth = Math.max(176, sectionWidth + 16);
                    sectionLeft = (baseWidth - sectionWidth) / 2;
                }
                rigStackStart = sectionLeft + EQUIPMENT_PREVIEW_PADDING;
                int rStartX = sectionLeft + previewSpace;
                int size = 0;
                for (int i = 0; i < rRows; ++i) {
                    for (int j = 0; j < rCols; ++j) {
                        rigsCustomInvY = 16 + i * 18;
                        if (j + i * rCols >= rigsSize) continue;
                        menu.addSlot(new ItemHandlerCopySlot(rigsContainer, j + i * rCols, rStartX + j * 18, rigsCustomInvY) {
                            @Override
                            public boolean mayPlace(@NotNull ItemStack stack) {
                                return !(stack.getItem() instanceof BaseBackpack || stack.getItem() instanceof BaseRigs)
                                        && super.mayPlace(stack);
                            }
                        });
                        size++;
                    }
                }
                rigsSlotEnd = backpackSlotEnd + size;
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
                    playerInvY = customInventory.backpackCustomInvY == 0 ? customInventory.rigsCustomInvY + 24 + i * 18 : Math.max(customInventory.backpackCustomInvY + 24 + i * 18, customInventory.rigsCustomInvY + 24 + i * 18);
                    customInventory.menu.addSlot(playerSlot(inventory, j + i * 9 + 9, 8 + j * 18, playerInvY));
                }
            }
            for (int j = 0; j < 9; ++j) {
                customInventory.menu.addSlot(playerSlot(inventory, j, 8 + j * 18, playerInvY + 24));
            }
        }
    }

}
