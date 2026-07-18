package net.zerocontact.network;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.client.menu.AmmoSelectorMenu;
import net.zerocontact.command.CommandManager;
import net.zerocontact.compat.MagazinesCompatHandler;
import net.zerocontact.events.EventUtil;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerAmmoSelector {
    public static ItemStack changedMagStack = ItemStack.EMPTY;

    public static void handleMenu(NetworkHandler.OpenAmmoSelectorPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player == null || player.isSpectator()) return;
        if (CommandManager.CommandSavedData.get((ServerLevel) player.level()).experimentalBallistic)
            context.enqueueWork(() -> {
                if (!IGun.mainHandHoldGun(player)) return;
                ItemStack gunStack = player.getMainHandItem();
                MagazinesCompatHandler handler = MagazinesCompatHandler.get();
                if (handler.getCompat().map(compat -> compat.isMagazineCompatibleWithGun(gunStack)).orElse(false))
                    return;
                LinkedHashMap<ItemStack, Integer> ammoMap = ServerAmmoSelector.getCreativeAmmoForHeldGun(player);
                NetworkHooks.openScreen(
                        player,
                        new SimpleMenuProvider((id, inv, __) -> new AmmoSelectorMenu(id, ammoMap, inv), Component.translatable("")),
                        buf -> buf.writeMap(ammoMap, FriendlyByteBuf::writeItem, FriendlyByteBuf::writeInt)
                );
            });
    }

    public static void handleSelected(NetworkHandler.SelectAmmoPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player == null || player.isSpectator()) return;
        context.enqueueWork(() -> {
            if (!IGun.mainHandHoldGun(player)) return;
            ItemStack gunStack = player.getMainHandItem();
            ItemStack selectedAmmoStack = msg.ammoItem();
            ResourceLocation selectedAmmoKey = ForgeRegistries.ITEMS.getKey(selectedAmmoStack.getItem());
            if (selectedAmmoKey == null) return;
            gunStack.getCapability(CapabilityRegistries.CARTRIDGE).ifPresent(cap -> cap.setClientSelectedAmmoVariant(gunStack, selectedAmmoKey.toString()));
            ModMessages.sendToPlayer(new NetworkHandler.ClientAmmoReloadPacket(), player);
        });
    }

    public static IItemHandler filteredAmmoHandler(IItemHandler raw, String selectedAmmoKey, ItemStack gunStack) {
        List<Integer> mappedSlots = new ArrayList<>();
        for (int i = 0; i < raw.getSlots(); i++) {
            ItemStack checkAmmoStack = raw.getStackInSlot(i);
            if (checkAmmoStack.getItem() instanceof IAmmo) {
                ResourceLocation stackKey = ForgeRegistries.ITEMS.getKey(checkAmmoStack.getItem());
                if (stackKey != null && stackKey.toString().equals(selectedAmmoKey)) {
                    mappedSlots.add(i);
                } else if (selectedAmmoKey.isEmpty()) {
                    ResourceLocation vanillaKey = ForgeRegistries.ITEMS.getKey(checkAmmoStack.getItem());
                    if (vanillaKey != null && vanillaKey.toString().equals("tacz:ammo")) {
                        mappedSlots.add(i);
                    }
                }
            } else if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunStack, checkAmmoStack)) {
                AmmoInjector.AmmoContext contextFromBox = AmmoInjector.read(checkAmmoStack);
                String stackKey = contextFromBox.caliber().variant();
                if (checkAmmoStack.getItem() instanceof AmmoBoxItem) {
                    if (stackKey.equals(selectedAmmoKey)) {
                        mappedSlots.add(i);
                    } else if (stackKey.isEmpty() && selectedAmmoKey.equals("tacz:ammo")) {
                        mappedSlots.add(i);
                    } else if(stackKey.isEmpty() &&iAmmoBox.isAllTypeCreative(checkAmmoStack)){
                        mappedSlots.add(i);
                    }
                } else if (MagazinesCompatHandler
                        .get()
                        .getCompat()
                        .map(compat -> compat.instanceOfMagazine(checkAmmoStack.getItem())).orElse(false)) {
                    mappedSlots.add(i);
                }
            }
        }
        //Reserve 2 extra slots for the magazine mixin to return a magazine that differs from the ones in the inventory.
        for (int j = 0; j < raw.getSlots(); j++) {
            if (raw.getStackInSlot(j).isEmpty()) {
                mappedSlots.add(j);
                if(mappedSlots.size()>=2){
                    break;
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
                ItemStack stack = raw.extractItem(mappedSlots.get(slot), amount, simulate);
                if (MagazinesCompatHandler
                        .get()
                        .getCompat()
                        .map(compat -> compat.instanceOfMagazine(stack.getItem())).orElse(false)) {
                    changedMagStack = stack;
                }
                return stack;
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
        if (!(entity instanceof ServerPlayer player) || player.isCreative()) return neededAmount;
        Consumer<ItemStack> paybackFunc = stack -> player.getInventory().placeItemBackInInventory(stack);
        if (rigs != null) {
            IItemHandler handler = rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(new ItemStackHandler());
            if (handler.getSlots() > 0) {
                paybackFunc = stack -> addToRigs(rigs, stack, handler, player.getInventory());
            }
        }
        CompoundTag gunTag = gunStack.getTag();
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return neededAmount;
        if (gunTag == null) return neededAmount;
        ICartridgeHolder cap = gunStack.getCapability(CapabilityRegistries.CARTRIDGE).resolve().orElse(null);
        if (cap == null) return neededAmount;
        String existedAmmoKey = cap.getAmmoVariantInGun(gunStack);
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

    private static void addToRigs(ItemStack rigs, ItemStack ammoStack, IItemHandler rigsHandler, Inventory playerInv) {
        ItemStack remain = ammoStack;
        for (int i = 0; i < rigsHandler.getSlots() && !remain.isEmpty(); i++) {
            remain = rigsHandler.insertItem(i, remain, false);
        }
        if (!remain.isEmpty()) {
            playerInv.placeItemBackInInventory(remain);
        }
        if (rigsHandler instanceof ItemStackHandler itemStackHandler && rigs != null) {
            rigs.getOrCreateTag().put("inventory", itemStackHandler.serializeNBT().getList("Items", Tag.TAG_COMPOUND));
        }
    }

    public static ItemStack getCreativeMagForHeldGun(ServerPlayer player) {
        ItemStack finalItem = ItemStack.EMPTY;
        ItemStack gunItem = player.getMainHandItem();
        IGun igun = IGun.getIGunOrNull(gunItem);
        if (igun == null) return finalItem;
        finalItem = MagazinesCompatHandler.get().getCompat().map(compat -> compat.getCompatibleMag(gunItem)).orElse(finalItem);
        return finalItem;
    }

    public static LinkedHashMap<ItemStack, Integer> getCreativeAmmoForHeldGun(ServerPlayer player) {
        Inventory vanillaInv = player.getInventory();
        ItemStack gunItem = player.getMainHandItem();
        LinkedHashMap<ItemWrapper, Integer> items = new LinkedHashMap<>();
        LinkedHashMap<ItemStack, Integer> finalItems = new LinkedHashMap<>();
        if (player.isCreative()) {
            IGun iGun = IGun.getIGunOrNull(gunItem);
            if (iGun != null) {
                TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem)).ifPresent((index) -> {
                    ResourceLocation defaultAmmoId = index.getGunData().getAmmoId();
                    ItemStack defaultAmmoStack = AmmoItemBuilder.create().setId(defaultAmmoId).setCount(1).build();
                    finalItems.put(defaultAmmoStack, 9999);
                });
                ForgeRegistries.ITEMS.getKeys()
                        .stream()
                        .map(ForgeRegistries.ITEMS::getValue)
                        .filter(GenerateAmmo.class::isInstance)
                        .map(GenerateAmmo.class::cast)
                        .filter(ammo -> ammo.isAmmoOfGun(gunItem, ammo.getDefaultInstance()))
                        .forEach(ammo -> items.merge(
                                new ItemWrapper(ammo, ammo.getAmmoId(ammo.getDefaultInstance()).toString()),
                                9999,
                                (l, r) -> r
                        ));
            }
        } else {
            for (ItemStack ammoStack : vanillaInv.items) {
                if (ammoStack.getItem() instanceof IAmmo ammo
                        && ammo.isAmmoOfGun(gunItem, ammoStack)) {
                    items.merge(
                            new ItemWrapper(ammoStack.getItem(), ammo.getAmmoId(ammoStack).toString()),
                            ammoStack.getCount(),
                            Integer::sum
                    );
                }
            }
            ItemStack rigs = EventUtil.getCuriosStackFirst(player, "rigs");
            rigs.getCapability(
                    ForgeCapabilities.ITEM_HANDLER,
                    null
            ).ifPresent(cap -> {
                for (int i = 0; i < cap.getSlots(); i++) {
                    ItemStack ammoStack = cap.getStackInSlot(i);
                    if (ammoStack.getItem() instanceof IAmmo ammo
                            && ammo.isAmmoOfGun(gunItem, ammoStack)) {
                        items.merge(
                                new ItemWrapper(ammoStack.getItem(), ammo.getAmmoId(ammoStack).toString()),
                                ammoStack.getCount(),
                                Integer::sum
                        );
                    }
                }
            });
        }
        finalItems.putAll((Map<? extends ItemStack, ? extends Integer>) items.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> {
                                    ItemStack stack = new ItemStack(entry.getKey().item, 1);
                                    stack.getOrCreateTag().putString("AmmoId", entry.getKey().ammoId());
                                    return stack;
                                },
                                Map.Entry::getValue,
                                Integer::sum,
                                LinkedHashMap::new
                        )
                ));
        return finalItems;
    }

    public record ItemWrapper(Item item, String ammoId) {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ItemWrapper wrapper && wrapper.item.equals(item);
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }
    }
}
