package net.zerocontact.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.caliber.ServerAmmoSelector;

import java.util.function.Supplier;

public record OpenAmmoSelectorPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static OpenAmmoSelectorPacket decode(FriendlyByteBuf buf) {
        return new OpenAmmoSelectorPacket();
    }

    public static void handle(OpenAmmoSelectorPacket msg, Supplier<NetworkEvent.Context> supplier) {
        ServerAmmoSelector.handleMenu(msg, supplier);
    }
}
