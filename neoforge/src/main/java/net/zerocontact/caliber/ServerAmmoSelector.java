package net.zerocontact.caliber;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zerocontact.caliber.api.ICartridgeHolder;
import net.zerocontact.caliber.capability.CapabilityRegistry;
import net.zerocontact.caliber.compat.ReloadManager;
import net.zerocontact.item.rigs.BaseRigs;
import net.zerocontact.menu.AmmoSelectorMenu;
import net.zerocontact.command.CommandManager;
import net.zerocontact.compat.MagazinesCompatHandler;
import net.zerocontact.item.ammo.GenerateAmmo;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.c2s.OpenAmmoSelectorPacket;
import net.zerocontact.network.c2s.SelectAmmoPacket;
import net.zerocontact.network.s2c.ClientAmmoReloadPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.zerocontact.caliber.CaliberSerializer.AI_AMMO;
import static net.zerocontact.caliber.CaliberSerializer.DEFAULT_AMMO;
import static net.zerocontact.caliber.CaliberSerializer.EXISTED_AMMO_ID;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerAmmoSelector {

    public interface MappedItemHandler extends IItemHandler {
        int toFilteredSlot(int rawSlot);
    }

    public static void handleMenu(OpenAmmoSelectorPacket msg, IPayloadContext context) {
        ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
        if (player == null || player.isSpectator()) return;
        if (CommandManager.CommandSavedData.get((ServerLevel) player.level()).experimentalBallistic)
            context.enqueueWork(() -> {
                if (!IGun.mainHandHoldGun(player)) return;
                ItemStack gunStack = player.getMainHandItem();
                MagazinesCompatHandler handler = MagazinesCompatHandler.get();
                if (handler.getCompat().map(compat -> compat.isMagazineCompatibleWithGun(gunStack)).orElse(false))
                    return;
                LinkedHashMap<ItemStack, Integer> ammoMap = ServerAmmoSelector.getCandidates(player);
                if (ammoMap.isEmpty()) return;
                player.openMenu(
                        new SimpleMenuProvider((id, inv, __) -> new AmmoSelectorMenu(id, ammoMap, inv), Component.translatable("")),
                        buf -> buf.writeMap(ammoMap,
                                (buffer, stack) -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack),
                                FriendlyByteBuf::writeInt)
                );
            });
    }


    public static void handleSelected(SelectAmmoPacket msg, IPayloadContext context) {
        ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
        if (player == null || player.isSpectator()) return;
        context.enqueueWork(() -> {
            if (!IGun.mainHandHoldGun(player)) return;
            ItemStack gunStack = player.getMainHandItem();
            ItemStack selectedAmmoStack = msg.ammoItem();
            if (selectedAmmoStack.isEmpty()) return;
            ResourceLocation selectedAmmoKey;
            if (selectedAmmoStack.getItem() instanceof GenerateAmmo) {
                selectedAmmoKey = BuiltInRegistries.ITEM.getKey(selectedAmmoStack.getItem());
            } else if (selectedAmmoStack.getItem() instanceof IAmmo ammo) {
                selectedAmmoKey = ammo.getAmmoId(selectedAmmoStack);
            } else {
                selectedAmmoKey = BuiltInRegistries.ITEM.getKey(selectedAmmoStack.getItem());
            }
            if (selectedAmmoKey == null) return;
            String selectedAmmoId = selectedAmmoKey.toString();
            ICartridgeHolder holder = gunStack.getCapability(CapabilityRegistry.CARTRIDGE);
            Optional.ofNullable(holder)
                    .ifPresent(cap -> cap.setClientSelectedAmmoVariant(gunStack, selectedAmmoId));
            ModMessages.sendToPlayer(
                    new ClientAmmoReloadPacket(player.getInventory().selected, selectedAmmoId),
                    player
            );
        });
    }


    public static IItemHandler filteredAmmoHandler(IItemHandler raw, String selectedAmmoKey, ItemStack gunStack) {
        // Fresh guns use an empty selection; default initialization writes the item ID,
        // not a data-driven ammo ID such as tacz:9mm. Both mean ordinary TaCZ ammo.
        boolean useDefaultAmmo = selectedAmmoKey.isEmpty() || DEFAULT_AMMO.equals(selectedAmmoKey);
        List<Integer> mappedSlots = new ArrayList<>();
        for (int i = 0; i < raw.getSlots(); i++) {
            ItemStack checkAmmoStack = raw.getStackInSlot(i);
            if (checkAmmoStack.getItem() instanceof IAmmo ammo) {
                ResourceLocation stackAmmoId = ammo.getAmmoId(checkAmmoStack);
                ResourceLocation stackItemId = BuiltInRegistries.ITEM.getKey(checkAmmoStack.getItem());
                boolean matchesSelectedAmmo;
                if (useDefaultAmmo) {
                    matchesSelectedAmmo = stackItemId.toString().equals(DEFAULT_AMMO);
                } else {
                    matchesSelectedAmmo =
                            stackAmmoId != null && stackAmmoId.toString().equals(selectedAmmoKey) || checkAmmoStack.getItem() instanceof GenerateAmmo && stackItemId.toString().equals(selectedAmmoKey);
                }
                if (matchesSelectedAmmo) {
                    mappedSlots.add(i);
                }
            } else if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox) {
                boolean boxedMagazine = MagazinesCompatHandler.get().getCompat()
                        .map(compat -> compat.hasCompatibleMagazineInBox(checkAmmoStack, gunStack))
                        .orElse(false);
                if (boxedMagazine) {
                    mappedSlots.add(i);
                    continue;
                }
                if (!iAmmoBox.isAmmoBoxOfGun(gunStack, checkAmmoStack)) continue;
                AmmoInjector.AmmoContext contextFromBox = AmmoInjector.get().readCxtFromStack(checkAmmoStack);
                String stackKey = contextFromBox.caliber().variant();
                if (checkAmmoStack.getItem() instanceof AmmoBoxItem) {
                    if (iAmmoBox.isAllTypeCreative(checkAmmoStack)) {
                        mappedSlots.add(i);
                    } else if (stackKey.equals(selectedAmmoKey)) {
                        mappedSlots.add(i);
                    } else if (useDefaultAmmo && (stackKey.isEmpty() || DEFAULT_AMMO.equals(stackKey))) {
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
        int emptyCounts = 0;
        for (int j = 0; j < raw.getSlots(); j++) {
            if (raw.getStackInSlot(j).isEmpty()) {
                if (emptyCounts >= 2) {
                    break;
                }
                mappedSlots.add(j);
                emptyCounts++;
            }
        }

        return new MappedItemHandler() {

            @Override
            public int toFilteredSlot(int rawSlot) {
                return mappedSlots.indexOf(rawSlot);
            }


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
        if (!(entity instanceof ServerPlayer player) || player.isCreative()) return neededAmount;
        Consumer<ItemStack> paybackFunc = stack -> player.getInventory().placeItemBackInInventory(stack);
        if (rigs != null) {
            IItemHandler handler = Optional.ofNullable(rigs.getCapability(Capabilities.ItemHandler.ITEM)).orElse(new ItemStackHandler());
            if (handler.getSlots() > 0) {
                paybackFunc = stack -> addToRigs(rigs, stack, handler, player.getInventory());
            }
        }
        CustomData data = gunStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return neededAmount;
        CompoundTag gunTag = data.copyTag();
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return neededAmount;
        if (gun.useInventoryAmmo(gunStack)) return neededAmount;
        ICartridgeHolder cap = gunStack.getCapability(CapabilityRegistry.CARTRIDGE);
        if (cap == null) return neededAmount;
        String existedAmmoKey = gunTag.getCompound(AI_AMMO)
                .getString(EXISTED_AMMO_ID);

        String newAmmoKey = AmmoInjector.get().getAmmoIdentity(newAmmoStack);

        if (existedAmmoKey.isEmpty() || newAmmoKey.isEmpty()) {
            return neededAmount;
        }

        if (!newAmmoKey.equals(existedAmmoKey)) {
            int currentAmmoCount = gun.getCurrentAmmoCount(gunStack);

            if (currentAmmoCount > 0) {
                ResourceLocation existedAmmoId =
                        ResourceLocation.tryParse(existedAmmoKey);

                Item existedAmmo = existedAmmoId == null
                        ? null
                        : BuiltInRegistries.ITEM.get(existedAmmoId);

                ItemStack refundStack;

                if (existedAmmo instanceof GenerateAmmo) {
                    refundStack = existedAmmo.getDefaultInstance();
                } else if (existedAmmo instanceof AmmoItem
                        || (existedAmmo != null && existedAmmo.equals(Items.AIR))
                        || existedAmmo == null) {
                    // TaCZ ammo IDs can come from resource packs and need not
                    // correspond to registered Forge items.
                    refundStack = AmmoItemBuilder.create()
                            .setId(existedAmmoId != null
                                    ? existedAmmoId
                                    : ResourceLocation.parse(existedAmmoKey))
                            .setCount(currentAmmoCount)
                            .build();
                } else {
                    refundStack = existedAmmo.getDefaultInstance();
                }

                refundStack.setCount(currentAmmoCount);
                paybackFunc.accept(refundStack);
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
        if (rigsHandler instanceof ItemStackHandler itemStackHandler && rigs != null && rigs.getItem() instanceof BaseRigs baseRigs) {
            baseRigs.manualSave(rigs,itemStackHandler,playerInv.player.level().registryAccess());
        }
    }

    public static LinkedHashMap<ItemStack, Integer> getCandidates(ServerPlayer player) {
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
                BuiltInRegistries.ITEM.keySet()
                        .stream()
                        .map(BuiltInRegistries.ITEM::get)
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
            ReloadManager.ReloadInventory inventory = ReloadManager.resolveReloadInv(player);
            IItemHandler handler = inventory.rawHandler();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack currentStack = handler.getStackInSlot(i);
                if (currentStack.getItem() instanceof IAmmo ammo
                        && ammo.isAmmoOfGun(gunItem, currentStack)) {
                    items.merge(
                            new ItemWrapper(currentStack.getItem(), ammo.getAmmoId(currentStack).toString()),
                            currentStack.getCount(),
                            Integer::sum
                    );
                }
            }
        }
        finalItems.putAll((Map<? extends ItemStack, ? extends Integer>) items.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> {
                                    ItemStack stack = new ItemStack(entry.getKey().item, 1);
                                    CustomData.update(DataComponents.CUSTOM_DATA,stack,(compoundTag)->
                                            compoundTag.putString("AmmoId", entry.getKey().ammoId()));
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
            return obj instanceof ItemWrapper(Item item1, String id)
                    && item.equals(item1)
                    && ammoId.equals(id);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(item, ammoId);
        }
    }

}
