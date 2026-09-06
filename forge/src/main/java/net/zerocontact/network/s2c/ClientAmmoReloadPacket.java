package net.zerocontact.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.client.network.ClientAmmoReloadHandler;

import java.util.function.Supplier;

public record ClientAmmoReloadPacket(int gunSlot, String selectedAmmoKey) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(gunSlot);
        buf.writeUtf(selectedAmmoKey);
    }

    public static ClientAmmoReloadPacket decode(FriendlyByteBuf buf) {
        return new ClientAmmoReloadPacket(buf.readInt(), buf.readUtf());
    }


    public static void handle(ClientAmmoReloadPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientAmmoReloadHandler.handle(packet)));
    }
}
