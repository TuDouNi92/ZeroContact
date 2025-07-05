package net.zerocontact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.ClientData;

import java.util.Optional;
import java.util.function.Supplier;

public class NetworkHandler {

    public static class SyncStaminaPacket {
        private final float stamina;

        public SyncStaminaPacket(float stamina) {
            this.stamina = stamina;
        }

        public SyncStaminaPacket(FriendlyByteBuf buf) {
            this.stamina = buf.readFloat();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeFloat(stamina);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> ClientData.setStamina(stamina));
        }
    }

    public record ToggleVisorPacket(boolean isToggle) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(isToggle);
        }
        public static ToggleVisorPacket decode(FriendlyByteBuf buf){
            return new ToggleVisorPacket(buf.readBoolean());
        }
        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                Optional<ItemStack> helmet = Optional.of(player.getItemBySlot(EquipmentSlot.HEAD));
                helmet.ifPresent(stack -> {
                    if (stack.getItem() instanceof Toggleable toggleable) {
                        boolean isEnabled = toggleable.getEnabled();
                        toggleable.setToggling(true);
                        ZeroContactLogger.LOG.info(isEnabled);
                        ModMessages.sendToPlayer(new ToggleVisorResultPacket(isEnabled), player);
                    }
                });
            });
        }
    }

    record ToggleVisorResultPacket(boolean lastSwitch) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(lastSwitch);
        }

        public static ToggleVisorResultPacket decode(FriendlyByteBuf buf) {
            return new ToggleVisorResultPacket(buf.readBoolean());
        }

        public static void handle(ToggleVisorResultPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> ClientData.setLastToggleVisorEnabled(msg.lastSwitch));
        }
    }
}
