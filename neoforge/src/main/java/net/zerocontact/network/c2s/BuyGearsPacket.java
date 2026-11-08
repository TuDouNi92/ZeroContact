package net.zerocontact.network.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.item.block.WorkBenchEntity;

import java.util.function.Supplier;

public record BuyGearsPacket(BlockPos pos, Item gearItem) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeItem(gearItem.getDefaultInstance());
    }

    public static BuyGearsPacket decode(FriendlyByteBuf buf) {
        return new BuyGearsPacket(buf.readBlockPos(), buf.readItem().getItem());
    }

    public static void handle(BuyGearsPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            WorkBenchEntity.buy(msg, player);
        });
    }

}
