package net.zerocontact.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.client.interaction.SuppressionManager;
import net.zerocontact.registries.ModSoundEventsReg;
import org.joml.Vector3f;

import java.util.function.Supplier;

public record AppendSuppressionPacket(Vector3f bulletPos, float amount) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVector3f(bulletPos);
        buf.writeFloat(amount);
    }

    public static AppendSuppressionPacket decode(FriendlyByteBuf buf) {
        return new AppendSuppressionPacket(buf.readVector3f(), buf.readFloat());
    }

    public static void handle(AppendSuppressionPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> SuppressionManager.increaseSuppression(msg.amount, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                minecraft.level.playLocalSound(
                        msg.bulletPos().x(),
                        msg.bulletPos().y(),
                        msg.bulletPos().z(),
                        ModSoundEventsReg.randomBulletSound(),
                        SoundSource.HOSTILE,
                        2.0F,
                        1.0F,
                        false
                );
            }
        }));
    }
}
