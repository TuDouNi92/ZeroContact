package net.zerocontact.network;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.events.AmmoInjector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServerAmmoSelector {
    public static void handle(NetworkHandler.SelectAmmoPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) return;
        context.enqueueWork(() -> {
            if (!IGun.mainHandHoldGun(player)) return;
            ItemStack gunStack = player.getMainHandItem();
            ItemStack selectedAmmoStack = msg.ammoItem();
            ResourceLocation selectedAmmoKey = ForgeRegistries.ITEMS.getKey(selectedAmmoStack.getItem());
            if (selectedAmmoKey == null) return;
            AmmoInjector.setClientSelectedAmmoVariant(gunStack,selectedAmmoKey.toString());
            ModMessages.sendToPlayer(new NetworkHandler.ClientAmmoReloadPacket(), player);
        });
    }

    public static IItemHandler filteredAmmoHandler(IItemHandler raw, String selectedAmmoKey) {
        List<Integer> mappedSlots = new ArrayList<>();
        for (int i = 0; i < raw.getSlots(); i++) {
            ItemStack stack = raw.getStackInSlot(i);
            ResourceLocation stackKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (stackKey != null && stackKey.toString().equals(selectedAmmoKey)) {
                mappedSlots.add(i);
            } else if (selectedAmmoKey.isEmpty()) {
                ResourceLocation vanillaKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (vanillaKey != null && vanillaKey.toString().equals("tacz:ammo")) {
                    mappedSlots.add(i);
                }
            }
        }

        return new IItemHandler() {
            @Override
            public int getSlots() {
                return mappedSlots.size();
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                return raw.getStackInSlot(mappedSlots.get(slot));
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return raw.insertItem(mappedSlots.get(slot), stack, simulate);
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return raw.extractItem(mappedSlots.get(slot), amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return raw.getSlotLimit(mappedSlots.get(slot));
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return raw.isItemValid(mappedSlots.get(slot), stack);
            }
        };
    }

    public static int dropAmmoFromGun(LivingEntity entity, ItemStack gunStack, ItemStack newAmmoStack, int neededAmount, @Nullable ItemStack rigs) {
        if (!(entity instanceof ServerPlayer player)) return neededAmount;
        Consumer<ItemStack> paybackFunc = stack -> player.getInventory().placeItemBackInInventory(stack);
        if (rigs != null && rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent()) {
            IItemHandler rigsHandler = (IItemHandler) rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).cast();
            paybackFunc = stack -> addToRigs(stack, rigsHandler, player.getInventory());
        }
        CompoundTag gunTag = gunStack.getTag();
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return neededAmount;
        if (gunTag == null) return neededAmount;
        String existedAmmoKey = AmmoInjector.getAmmoVariantInGun(gunStack);
        Item existedAmmo = ForgeRegistries.ITEMS.getValue(new ResourceLocation(existedAmmoKey));
        if (existedAmmo == null) return neededAmount;
        if (!newAmmoStack.is(existedAmmo)) {
            int currentAmmoCount = gun.getCurrentAmmoCount(gunStack);
            if (existedAmmo instanceof AmmoItem || existedAmmo.equals(Items.AIR)) {
                Consumer<ItemStack> finalPaybackFunc = paybackFunc;
                TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack)).ifPresent(index -> {
                    ResourceLocation vanillaAmmoIdResource = index.getGunData().getAmmoId();
                    ItemStack vanillaAmmoStack = AmmoItemBuilder.create().setId(vanillaAmmoIdResource).setCount(currentAmmoCount).build();
                    if (currentAmmoCount == 0 && vanillaAmmoStack.getCount() != currentAmmoCount) {
                        vanillaAmmoStack.setCount(currentAmmoCount);
                    }
                    finalPaybackFunc.accept(vanillaAmmoStack);
                });
            } else {
                ItemStack defaultInstanceStack = existedAmmo.getDefaultInstance();
                defaultInstanceStack.setCount(currentAmmoCount);
                paybackFunc.accept(defaultInstanceStack);
            }
            neededAmount += currentAmmoCount;
            gun.setCurrentAmmoCount(gunStack, 0);
        }
        return neededAmount;
    }

    private static void addToRigs(ItemStack stack, IItemHandler rigsHandler, Inventory playerInv) {
        ItemStack remain = stack;
        for (int i = 0; i < rigsHandler.getSlots() && !remain.isEmpty(); i++) {
            remain = rigsHandler.insertItem(i, remain, false);
        }
        if (!remain.isEmpty()) {
            playerInv.placeItemBackInInventory(remain);
        }
    }

    public static boolean isNeededAmmo(ItemStack checkAmmoStack, ItemStack gunStack) {
        String ammoId = AmmoInjector.getAmmoVariantInGun(gunStack);
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ammoId));
        if (ammoItem == null) return false;
        return checkAmmoStack.is(ammoItem);
    }
}
