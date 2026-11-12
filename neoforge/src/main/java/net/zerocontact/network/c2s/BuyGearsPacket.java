package net.zerocontact.network.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.item.block.WorkBenchEntity;
import net.zerocontact.item.block.menu.WorkbenchMenu;

public record BuyGearsPacket(BlockPos pos, Item gearItem) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BuyGearsPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "buy_gears"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BuyGearsPacket> STREAM_CODEC =
            StreamCodec.ofMember(BuyGearsPacket::encode, BuyGearsPacket::decode);

    @Override
    public CustomPacketPayload.Type<BuyGearsPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, gearItem.getDefaultInstance());
    }

    public static BuyGearsPacket decode(RegistryFriendlyByteBuf buf) {
        return new BuyGearsPacket(buf.readBlockPos(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf).getItem());
    }

    public static void handle(BuyGearsPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
            if (player == null || player.isDeadOrDying() || player.isRemoved() || player.isSpectator()) return;
            if (!player.canInteractWithBlock(msg.pos, 0.0)) return;
            if (!(player.containerMenu instanceof WorkbenchMenu menu)
                    || menu.blockEntity == null
                    || !menu.blockEntity.equals(player.level().getBlockEntity(msg.pos())))
                return;
            WorkBenchEntity.buy(msg, player);
        });
    }

}
