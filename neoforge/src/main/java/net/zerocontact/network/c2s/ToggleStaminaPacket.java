package net.zerocontact.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.command.CommandManager;

import java.util.Optional;
import java.util.function.Supplier;

public record ToggleStaminaPacket(boolean isEnable) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isEnable);
    }

    public static ToggleStaminaPacket decode(FriendlyByteBuf buf) {
        return new ToggleStaminaPacket(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            Optional.ofNullable(serverPlayer).ifPresent(serverPlayer1 -> {
                CommandManager.CommandSavedData data = CommandManager.CommandSavedData.get(serverPlayer1.serverLevel());
                data.setStaminaState(isEnable);
            });
        });
    }
}
