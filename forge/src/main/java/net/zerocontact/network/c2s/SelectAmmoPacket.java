package net.zerocontact.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.caliber.ServerAmmoSelector;

import java.util.function.Supplier;

public record SelectAmmoPacket(ItemStack ammoItem) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(ammoItem);
    }

    public static SelectAmmoPacket decode(FriendlyByteBuf buf) {
        return new SelectAmmoPacket(buf.readItem());
    }

    public static void handle(SelectAmmoPacket msg, Supplier<NetworkEvent.Context> supplier) {
        ServerAmmoSelector.handleSelected(msg, supplier);
    }
}
