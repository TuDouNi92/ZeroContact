package net.zerocontact.client.menu;

import com.tacz.guns.api.item.IAmmo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.zerocontact.forge_registries.ModMenus;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.stream.Collectors;

public class AmmoSelectorMenu extends AbstractContainerMenu {
    public final ArrayList<Map.Entry<ItemStack, Integer>> ammo;
    public final Inventory playerInv;

    public AmmoSelectorMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, buf.readMap(FriendlyByteBuf::readItem, FriendlyByteBuf::readInt), playerInv);
    }

    //server Constructor
    public AmmoSelectorMenu(int containerId, Map<ItemStack, Integer> ammo, Inventory inventory) {
        super(ModMenus.AMMO_SELECTOR.get(), containerId);
        this.ammo = new ArrayList<>(ammo.entrySet());
        this.playerInv = inventory;
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

    public static LinkedHashMap<ItemStack, Integer> getAmmoCount(ServerPlayer player) {
        Inventory vanillaInv = player.getInventory();
        ItemStack gunItem = player.getMainHandItem();
        LinkedHashMap<ItemWrapper, Integer> items = new LinkedHashMap<>();
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
        CuriosApi.getCuriosInventory(player).ifPresent(
                itemHandler -> itemHandler.getStacksHandler("rigs")
                        .ifPresent(iCurioStacksHandler -> {
                            ItemStack rigs = iCurioStacksHandler
                                    .getStacks()
                                    .getStackInSlot(0);
                            rigs.getCapability(
                                    ForgeCapabilities.ITEM_HANDLER,
                                    null
                            ).ifPresent(cap -> {
                                for (int i = 0; i < cap.getSlots(); i++) {
                                    ItemStack ammoStack =
                                            cap.getStackInSlot(i);
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
                        }));
        LinkedHashMap<ItemStack, Integer> map = items.entrySet()
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
                );
        return map;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
