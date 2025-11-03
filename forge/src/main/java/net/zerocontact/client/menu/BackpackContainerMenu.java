package net.zerocontact.client.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.container.IndexSimpleContainer;
import net.zerocontact.forge_registries.ModMenus;
import net.zerocontact.item.backpack.BaseBackpack;
import net.zerocontact.item.rigs.BaseRigs;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class BackpackContainerMenu extends AbstractContainerMenu {
    private IndexSimpleContainer backpackContainer = new IndexSimpleContainer(0);
    private IndexSimpleContainer rigsContainer =new IndexSimpleContainer(0);
    private final TriggerSource triggerSource;
    public int minSlotX = Integer.MAX_VALUE;
    public int maxSlotX = Integer.MIN_VALUE;
    public int minSlotY = Integer.MAX_VALUE;
    public int maxSlotY = Integer.MIN_VALUE;
    public int guiWidth,guiHeight;
    public ItemStack backpackRenderStack;
    public ItemStack rigsRenderStack;

    public enum TriggerSource {
        USE,
        KEY
    }

    public BackpackContainerMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readEnum(TriggerSource.class));
    }

    public BackpackContainerMenu(int containerId, Inventory playerInv, TriggerSource source) {
        super(ModMenus.BACKPACK_CONTAINER.get(), containerId);
        this.triggerSource = source;
        if (triggerSource == BackpackContainerMenu.TriggerSource.USE) {
            backpackRenderStack = getHandStack(playerInv.player);
            if(backpackRenderStack.getItem() instanceof BaseRigs baseRigs){
                rigsContainer = new IndexSimpleContainer(baseRigs.containerSize);
            } else if (backpackRenderStack.getItem() instanceof BaseBackpack backpack) {
                backpackContainer = new IndexSimpleContainer(backpack.containerSize);
            }
            readInvfromTag(backpackRenderStack);
        } else if (source == BackpackContainerMenu.TriggerSource.KEY) {
            CuriosApi.getCuriosInventory(playerInv.player).ifPresent(inventoryHandler -> {
                inventoryHandler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                    ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                    if(backpackStack.getItem() instanceof BaseBackpack backpack) {
                        backpackContainer = new IndexSimpleContainer(backpack.containerSize);
                        backpackRenderStack = backpackStack;
                        readInvfromTag(backpackStack);
                    }
                });
                inventoryHandler.getStacksHandler("rigs").ifPresent(stacksHandler -> {
                    ItemStack rigsStack = stacksHandler.getStacks().getStackInSlot(0);
                    if(rigsStack.getItem() instanceof BaseRigs baseRigs) {
                        rigsContainer = new IndexSimpleContainer(baseRigs.containerSize);
                        rigsRenderStack = rigsStack;
                        readInvfromTag(rigsStack);
                    }
                });
            });

        }
        CustomInventory customInventory = new CustomInventory(this, backpackContainer.getContainerSize(), rigsContainer.getContainerSize());
        new PlayerInventory(playerInv, customInventory);
        int padding = 8;
        this.guiWidth = (maxSlotX - minSlotX) + padding * 2;
        this.guiHeight = (maxSlotY - minSlotY) + padding * 2;
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

    private void readInvfromTag(ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BaseRigs) {
            Optional<CompoundTag> inventoryTag = Optional.ofNullable(backpackStack.getTag());
            inventoryTag.ifPresent(inventoryTag1 -> {
                ListTag listTag = inventoryTag1.getList("inventory", Tag.TAG_COMPOUND);
                rigsContainer.fromIndexTag(listTag);
            });
        } else {
            if (backpackStack.getItem() instanceof BaseBackpack) {
                Optional<CompoundTag> inventoryTag = Optional.ofNullable(backpackStack.getTag());
                inventoryTag.ifPresent(inventoryTag1 -> {
                    ListTag listTag = inventoryTag1.getList("inventory", Tag.TAG_COMPOUND);
                    backpackContainer.fromIndexTag(listTag);
                });
            }
        }
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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    class CustomInventory {
        protected int backpackCustomInvY;
        protected int rigsCustomInvY;
        protected BackpackContainerMenu menu;
        private int startX;
        CustomInventory(BackpackContainerMenu menu,  int backpackSize,  int rigsSize) {
            this.menu = menu;
            addCustomInventory(backpackSize, rigsSize);
        }

        private void addCustomInventory(int backpackSize, int rigsSize) {
            //Backpack part
            int backpackRightScreenX=0;
            if (backpackSize != 0) {
                int cols = Mth.ceil(Mth.sqrt(backpackSize));
                int rows = Mth.ceil((double) backpackSize / cols);
                startX = (176 - cols * 18) / 2;
                for (int i = 0; i < rows; ++i) {
                    for (int j = 0; j < cols; ++j) {
                        backpackCustomInvY = 16 + i * 18;
                        menu.addSlot(new Slot(backpackContainer, j + i * cols, startX + j * 18, backpackCustomInvY) {
                            @Override
                            public boolean mayPlace(@NotNull ItemStack stack) {
                                return !(stack.getItem() instanceof BaseBackpack);
                            }
                        });
                        backpackRightScreenX = startX + j * 36;
                    }
                }
            }
            //Rigs part
            if (rigsSize != 0) {
                int rCols = Mth.ceil(Mth.sqrt(rigsSize));
                int rRows = Mth.ceil((double) rigsSize / rCols);
                int rStartX = startX==0?(176 - rCols * 18) / 2: backpackRightScreenX+16;
                for (int i = 0; i < rRows; ++i) {
                    for (int j = 0; j < rCols; ++j) {
                        rigsCustomInvY = 16 + i * 18;
                        menu.addSlot(new Slot(rigsContainer, j + i * rCols, rStartX + j * 18, rigsCustomInvY) {
                            @Override
                            public boolean mayPlace(@NotNull ItemStack stack) {
                                return !(stack.getItem() instanceof BaseBackpack || stack.getItem() instanceof BaseRigs);
                            }
                        });
                    }
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
                    playerInvY = customInventory.backpackCustomInvY ==0?customInventory.rigsCustomInvY+24+i*18:customInventory.backpackCustomInvY + 24 + i * 18;
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
            writeInvToTag(getHandStack(serverPlayer));
        } else if (triggerSource == TriggerSource.KEY) {
            CuriosApi.getCuriosInventory(player).ifPresent(inventoryHandler -> inventoryHandler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                writeInvToTag(backpackStack);
            }));
            CuriosApi.getCuriosInventory(player).ifPresent(inventoryHandler -> inventoryHandler.getStacksHandler("rigs").ifPresent(stacksHandler -> {
                ItemStack rigsStack = stacksHandler.getStacks().getStackInSlot(0);
                writeInvToTag(rigsStack);
            }));
        }
        super.removed(player);
    }

    private void writeInvToTag(ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BaseRigs) {
            backpackStack.getOrCreateTag().put("inventory", rigsContainer.createIndexTag());
        } else {
            if (backpackStack.getItem() instanceof BaseBackpack) {
                backpackStack.getOrCreateTag().put("inventory", backpackContainer.createIndexTag());
            }
        }
    }
}
