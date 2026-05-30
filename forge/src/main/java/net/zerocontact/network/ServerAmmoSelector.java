package net.zerocontact.network;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
            gunStack
                    .getOrCreateTagElement("ai_ammo")
                    .putString("selected_variant", selectedAmmoKey.toString());
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

    public static int dropAmmoFromGun(LivingEntity entity, ItemStack gunStack, ItemStack newAmmoStack, int neededAmount) {
        if (!(entity instanceof ServerPlayer player)) return neededAmount;
        CompoundTag gunTag = gunStack.getTag();
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return neededAmount;
        if (gunTag == null) return neededAmount;
        String existedAmmoKey = gunTag.getCompound("ai_ammo").getString("existed_variant");
        Item existedAmmo = ForgeRegistries.ITEMS.getValue(new ResourceLocation(existedAmmoKey));
        if (existedAmmo == null) return neededAmount;
        if (!newAmmoStack.is(existedAmmo)) {
            int currentAmmoCount = gun.getCurrentAmmoCount(gunStack);
            if (existedAmmo instanceof AmmoItem || existedAmmo.equals(Items.AIR)) {
                TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack)).ifPresent(index -> {
                    ResourceLocation vanillaAmmoIdResource = index.getGunData().getAmmoId();
                    ItemStack vanillaAmmoStack = AmmoItemBuilder.create().setId(vanillaAmmoIdResource).setCount(currentAmmoCount).build();
                    if (currentAmmoCount == 0 && vanillaAmmoStack.getCount() != currentAmmoCount) {
                        vanillaAmmoStack.setCount(currentAmmoCount);
                    }
                    player.getInventory().add(vanillaAmmoStack);
                });
            } else {
                ItemStack defaultInstanceStack = existedAmmo.getDefaultInstance();
                defaultInstanceStack.setCount(currentAmmoCount);
                player.getInventory().add(defaultInstanceStack);
            }
            neededAmount += currentAmmoCount;
            gun.setCurrentAmmoCount(gunStack, 0);
        }
        return neededAmount;
    }

    public static boolean isNeededAmmo(ItemStack checkAmmoStack, ItemStack gunStack) {
        String ammoId = gunStack.getOrCreateTag().getCompound("ai_ammo").getString("existed_variant");
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ammoId));
        if (ammoItem == null) return false;
        return checkAmmoStack.is(ammoItem);
    }
}
