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
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.menu.BackpackContainerMenu;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Optional;

public abstract class BaseBackpack extends AbstractGenerateGeoCurioItemImpl implements ArmorTypeTag, Toggleable.Backpack {
    public final int containerSize;
    private boolean canOpen = false;

    public BaseBackpack(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super(id, defaultDurability, texture, model, animation);
        this.containerSize = containerSize;
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.BACKPACK;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (canOpen && !slotContext.entity().level().isClientSide() && slotContext.entity() instanceof ServerPlayer serverPlayer) {
            callOpenScreen(serverPlayer, BackpackContainerMenu.TriggerSource.KEY);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            callOpenScreen(serverPlayer, BackpackContainerMenu.TriggerSource.USE);
        }
        return super.use(level, player, usedHand);
    }

    private void callOpenScreen(ServerPlayer serverPlayer, BackpackContainerMenu.TriggerSource source) {
        NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inv, __) -> new BackpackContainerMenu(id, inv, source), Component.translatable(this.getDescriptionId())), buf -> {
            buf.writeEnum(source);
        });
        canOpen = false;
    }

    @Override
    public void setToggling(boolean open) {
        this.canOpen = open;
    }

    @Override
    public boolean getToggling() {
        return this.canOpen;
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
}
