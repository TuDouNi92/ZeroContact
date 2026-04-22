package net.zerocontact.item.backpack;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.menu.BackpackContainerMenu;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Optional;

import static net.zerocontact.events.EventUtil.getAllyPlayer;
import static net.zerocontact.events.EventUtil.isLookAtTargetBack;

public abstract class BaseBackpack extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag, Toggleable.Backpack {
    public final int containerSize;

    public BaseBackpack(ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super("", 0, texture, model, animation);
        this.containerSize = containerSize;
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return IEquipmentTypeTag.EquipmentType.BACKPACK;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (getToggling(stack) && !slotContext.entity().level().isClientSide() && slotContext.entity() instanceof ServerPlayer serverPlayer) {
            callOpenScreen(serverPlayer, BackpackContainerMenu.TriggerSource.KEY,ItemStack.EMPTY);
            setToggling(stack,false);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            callOpenScreen(serverPlayer, BackpackContainerMenu.TriggerSource.USE,ItemStack.EMPTY);
        }
        return super.use(level, player, usedHand);
    }

    public void callOpenScreen(ServerPlayer visitor, BackpackContainerMenu.TriggerSource source, ItemStack allieStack) {
        NetworkHooks.openScreen(visitor, new SimpleMenuProvider((id, inv, __) -> new BackpackContainerMenu(id, inv, source, allieStack), Component.translatable(this.getDescriptionId())), buf -> {
            buf.writeEnum(source);
            buf.writeItem(allieStack);
        });
    }

    @Override
    public void setToggling(ItemStack stack, boolean isOpen) {
        stack.getOrCreateTag().putBoolean("canOpen", isOpen);
    }

    @Override
    public boolean getToggling(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("canOpen");
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    private ListTag readInvTags(ItemStack stack) {
        if (stack.getItem() instanceof BaseBackpack) {
            Optional<CompoundTag> inventoryTag = Optional.ofNullable(stack.getTag());
            if (inventoryTag.isPresent()) {
                return inventoryTag.get().getList("inventory", Tag.TAG_COMPOUND);
            }
        }
        return new ListTag();
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
                ListTag listTag = readInvTags(stack);
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.put("Items", listTag);
                ItemStackHandler container = new ItemStackHandler(listTag.size());
                container.deserializeNBT(compoundTag);
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> container));
            }
        };
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity().isCrouching()) {
            return true;
        }
        return super.canEquipFromUse(slotContext, stack);
    }

    public static void whetherOpenAllyScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayer targetEntity = getAllyPlayer(player);
            if (isLookAtTargetBack(serverPlayer, targetEntity) && player.isCrouching()) {
                CuriosApi.getCuriosInventory(targetEntity)
                        .ifPresent(itemHandler ->
                                itemHandler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                                    ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                                    if (backpackStack.getItem() instanceof BaseBackpack backpack && player.isCrouching())
                                        backpack.callOpenScreen(serverPlayer, BackpackContainerMenu.TriggerSource.ALLY, backpackStack);
                                }));
            }
        }

    }
}
