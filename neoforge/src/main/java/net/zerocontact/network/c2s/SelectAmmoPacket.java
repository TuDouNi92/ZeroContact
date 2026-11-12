package net.zerocontact.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.caliber.ServerAmmoSelector;

public record SelectAmmoPacket(ItemStack ammoItem) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectAmmoPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "select_ammo"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectAmmoPacket> STREAM_CODEC =
            StreamCodec.ofMember(SelectAmmoPacket::encode, SelectAmmoPacket::decode);

    @Override
    public CustomPacketPayload.Type<SelectAmmoPacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, ammoItem);
    }

    public static SelectAmmoPacket decode(RegistryFriendlyByteBuf buf) {
        return new SelectAmmoPacket(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    public static void handle(SelectAmmoPacket msg, IPayloadContext context) {
        ServerAmmoSelector.handleSelected(msg, context);
    }
}
