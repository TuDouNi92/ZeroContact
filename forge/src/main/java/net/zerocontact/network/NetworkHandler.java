package net.zerocontact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.client.ClientStaminaData;

import java.util.function.Supplier;

public class NetworkHandler {
    public static class SyncStaminaPacket {
        private final float stamina;

        public SyncStaminaPacket(float stamina) {
            this.stamina = stamina;
        }

        public SyncStaminaPacket(FriendlyByteBuf buf) {
            this.stamina =buf.readFloat();
        }
        public void toBytes(FriendlyByteBuf buf){
            buf.writeFloat(stamina);
        }
        public void handle(Supplier<NetworkEvent.Context> supplier){
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(()-> ClientStaminaData.setStamina(stamina));
        }
    }
}
