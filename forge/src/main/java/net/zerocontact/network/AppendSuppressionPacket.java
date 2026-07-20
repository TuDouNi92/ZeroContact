package net.zerocontact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.client.interaction.SuppressionManager;

import java.util.function.Supplier;

public record AppendSuppressionPacket(float amount) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(amount);
    }

    public static AppendSuppressionPacket decode(FriendlyByteBuf buf) {
        return new AppendSuppressionPacket(buf.readFloat());
    }

    public static void handle(AppendSuppressionPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> SuppressionManager.increaseSuppression(msg.amount));
    }
}
