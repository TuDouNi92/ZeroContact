package net.zerocontact.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.api.Toggleable;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.s2c.ToggleVisorResultPacket;

import java.util.Optional;
import java.util.function.Supplier;

public record FlipVisorPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static FlipVisorPacket decode(FriendlyByteBuf buf) {
        return new FlipVisorPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Optional<ItemStack> helmet = Optional.of(player.getItemBySlot(EquipmentSlot.HEAD));
            helmet.ifPresent(stack -> {
                if (stack.getItem() instanceof Toggleable toggleable) {
                    toggleable.flipState(toggleable, stack);
                    ModMessages.sendToPlayer(new ToggleVisorResultPacket(toggleable.readAnimData(stack)), player);
                }
            });
        });
    }
}
