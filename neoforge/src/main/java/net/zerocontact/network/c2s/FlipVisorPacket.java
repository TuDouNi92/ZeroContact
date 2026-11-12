package net.zerocontact.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.api.armor.Toggleable;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.s2c.ToggleVisorResultPacket;

import java.util.Optional;

public record FlipVisorPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FlipVisorPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "flip_visor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlipVisorPacket> STREAM_CODEC =
            StreamCodec.ofMember(FlipVisorPacket::encode, FlipVisorPacket::decode);

    @Override
    public CustomPacketPayload.Type<FlipVisorPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
    }

    public static FlipVisorPacket decode(RegistryFriendlyByteBuf buf) {
        return new FlipVisorPacket();
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
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
