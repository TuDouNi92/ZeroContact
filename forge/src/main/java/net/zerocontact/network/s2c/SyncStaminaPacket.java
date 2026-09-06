package net.zerocontact.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.client.ClientData;

import java.util.function.Supplier;

public class SyncStaminaPacket {
    private final float stamina;
    private final boolean enabled;

    public SyncStaminaPacket(float stamina, boolean enabled) {
        this.stamina = stamina;
        this.enabled = enabled;
    }

    public SyncStaminaPacket(FriendlyByteBuf buf) {
        this.stamina = buf.readFloat();
        this.enabled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(stamina);
        buf.writeBoolean(enabled);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientData.setStamina(stamina, enabled));
    }
}
