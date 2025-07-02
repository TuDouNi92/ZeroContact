package net.zerocontact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.api.Togglable;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.interaction.ToggleInteraction;

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

    public static class ToggleVisorPacket {
        public ToggleVisorPacket() {
        }

        public ToggleVisorPacket(FriendlyByteBuf buf) {

        }

        public void toBytes(FriendlyByteBuf buf) {

        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                Optional<ItemStack> helmet = Optional.of(player.getItemBySlot(EquipmentSlot.HEAD));
                helmet.ifPresent(stack -> {
                    if (!(stack.getItem() instanceof Togglable togglable)) return;
                    Item item = togglable.getToggleBrother();
                    Optional<ItemStack> newStack = ToggleInteraction.toggleHelmetVisor(stack, item, item.getClass().getSuperclass());
                    newStack.ifPresentOrElse(stack1 -> {
                        stack1.setTag(stack.getTag());
                        player.setItemSlot(EquipmentSlot.HEAD, stack1);
                        ModMessages.sendToPlayer(new ToggleVisorResultPacket(!togglable.isVisor()), player);
                    }, () -> ModMessages.sendToPlayer(new ToggleVisorResultPacket(false), player));
                });
            });
        }
    }

    record ToggleVisorResultPacket(boolean success) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(success);
        }

        public static ToggleVisorResultPacket decode(FriendlyByteBuf buf) {
            return new ToggleVisorResultPacket(buf.readBoolean());
        }

        public static void handle(ToggleVisorResultPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> ClientData.setLastToggleVisorSuccess(msg.success));
        }
    }
}
