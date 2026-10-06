package net.zerocontact.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.forge_registries.MenuRegistry;
import net.zerocontact.item.backpack.BaseBackpack;
import net.zerocontact.item.rigs.BaseRigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public class BackpackContainerMenu extends AbstractContainerMenu {
    private Container backpackContainer = new SimpleContainer(0);
    private Container rigsContainer = new SimpleContainer(0);
    private final TriggerSource triggerSource;
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
    public BackpackContainerMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readEnum(TriggerSource.class), buf.readItem());
    }

    //server side
    public BackpackContainerMenu(int containerId, Inventory playerInv, TriggerSource source, @Nullable ItemStack allyStack) {
        super(MenuRegistry.BACKPACK_CONTAINER.get(), containerId);
        this.triggerSource = source;
        if (triggerSource == TriggerSource.USE) {
            backpackRenderStack = getHandStack(playerInv.player);
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

        CustomInventory customInventory = new CustomInventory(this, backpackContainer.getContainerSize(), rigsContainer.getContainerSize());
        new PlayerInventory(playerInv, customInventory);
        int padding = 8;
        this.guiWidth = (maxSlotX - minSlotX) + padding * 2;
        this.guiHeight = (maxSlotY - minSlotY) + (padding + 4) * 2;
    }

    private ItemStack getHandStack(Player player) {
        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);
        if (mainHandStack == ItemStack.EMPTY && offHandStack == ItemStack.EMPTY) {
            return ItemStack.EMPTY;
        } else {
            return mainHandStack == ItemStack.EMPTY ? offHandStack : mainHandStack;
        }
    }

    private void bindInventory(ItemStack stack) {
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if (!(handler instanceof Container container)) return;
            if (stack.getItem() instanceof BaseRigs) {
                rigsContainer = container;
            } else if (stack.getItem() instanceof BaseBackpack) {
                backpackContainer = container;
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
        ItemStack copy = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) return copy;
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
    public boolean stillValid(@NotNull Player player) {
        return true;
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
                        menu.addSlot(new Slot(backpackContainer, j + i * cols, startX + j * 18, backpackCustomInvY) {
                            @Override
                            public boolean mayPlace(@NotNull ItemStack stack) {
                                return !(stack.getItem() instanceof BaseBackpack);
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
                int baseWidth = Math.max(176, rigsWidth);
                int slotXStartFix = maxSlotX < 1 ? (baseWidth - rigsWidth) / 2 : maxSlotX;
                int rStartX = maxSlotX < 1 ? slotXStartFix : slotXStartFix + 24;
                int size = 0;
                rigStackStart = slotXStartFix;
                for (int i = 0; i < rRows; ++i) {
                    for (int j = 0; j < rCols; ++j) {
                        rigsCustomInvY = 16 + i * 18;
                        if (j + i * rCols >= rigsSize) continue;
                        menu.addSlot(new Slot(rigsContainer, j + i * rCols, rStartX + j * 18, rigsCustomInvY) {
                            @Override
                            public boolean mayPlace(@NotNull ItemStack stack) {
                                return !(stack.getItem() instanceof BaseBackpack || stack.getItem() instanceof BaseRigs);
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
                    customInventory.menu.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, playerInvY)
                    );
                }
            }
            for (int j = 0; j < 9; ++j) {
                customInventory.menu.addSlot(new Slot(inventory, j, 8 + j * 18, playerInvY + 24));
            }
        }
    }

}
