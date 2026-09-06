package net.zerocontact.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.client.animation.VisorTracker;

import java.util.function.Supplier;

public record ToggleVisorResultPacket(AnimateData.VisorAnimateData visorAnimateData) {
    public void encode(FriendlyByteBuf buf) {
        String animName = "";
        boolean isPlaying = false;
        double animLength = 0;
        if (visorAnimateData != null) {
            animName = visorAnimateData.animationName;
            isPlaying = visorAnimateData.isPlaying;
            animLength = visorAnimateData.animLength;
        }
        buf.writeUtf(animName);
        buf.writeDouble(animLength);
        buf.writeBoolean(isPlaying);
    }

    public static ToggleVisorResultPacket decode(FriendlyByteBuf buf) {
        return new ToggleVisorResultPacket(
                new AnimateData.VisorAnimateData(
                        buf.readUtf(),
                        buf.readDouble(),
                        buf.readBoolean()
                )
        );
    }

    public static void handle(ToggleVisorResultPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> VisorTracker.update(msg.visorAnimateData));
    }
}
