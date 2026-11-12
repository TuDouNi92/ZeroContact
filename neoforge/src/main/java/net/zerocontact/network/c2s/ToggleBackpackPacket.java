package net.zerocontact.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.item.backpack.BaseBackpack;
import top.theillusivec4.curios.api.CuriosApi;

public record ToggleBackpackPacket(boolean toggle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleBackpackPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "toggle_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleBackpackPacket> STREAM_CODEC =
            StreamCodec.ofMember(ToggleBackpackPacket::encode, ToggleBackpackPacket::decode);

    @Override
    public CustomPacketPayload.Type<ToggleBackpackPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(toggle);
    }

    public static ToggleBackpackPacket decode(RegistryFriendlyByteBuf buf) {
        return new ToggleBackpackPacket(buf.readBoolean());
    }

    public static void handle(ToggleBackpackPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
            if (player == null) return;
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getStacksHandler(CuriosConstants.BACKPACK).ifPresent(stacksHandler -> {
                    ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                    if (backpackStack.getItem() instanceof BaseBackpack backpack) {
                        backpack.setToggling(backpackStack, msg.toggle);
                    }
                });
                handler.getStacksHandler(CuriosConstants.RIGS).ifPresent(stacksHandler -> {
                    ItemStack rigsStack = stacksHandler.getStacks().getStackInSlot(0);
                    if (rigsStack.getItem() instanceof BaseBackpack rigs) {
                        rigs.setToggling(rigsStack, msg.toggle);
                    }
                });
            });
        });
    }
}
