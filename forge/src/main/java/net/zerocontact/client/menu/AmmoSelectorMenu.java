package net.zerocontact.client.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.forge_registries.ModMenus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AmmoSelectorMenu extends AbstractContainerMenu {
    public final ArrayList<Map.Entry<ItemStack, Integer>> ammo;
    public final Inventory playerInv;

    public AmmoSelectorMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, buf.readMap(LinkedHashMap::new,FriendlyByteBuf::readItem, FriendlyByteBuf::readInt), playerInv);
    }

    //server Constructor
    public AmmoSelectorMenu(int containerId, LinkedHashMap<ItemStack, Integer> ammo, Inventory inventory) {
        super(ModMenus.AMMO_SELECTOR.get(), containerId);
        this.ammo = new ArrayList<>(ammo.entrySet());
        this.playerInv = inventory;
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
