package net.zerocontact.armor.modular.client.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public final class ItemStackCodec {
    private ItemStackCodec() {
    }

    public static void write(FriendlyByteBuf buf, ItemStack stack) {
        buf.writeNbt(stack.save(new CompoundTag()));
    }

    public static ItemStack read(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag == null) {
            throw new DecoderException("Missing ItemStack NBT");
        }
        return ItemStack.of(tag);
    }
}
