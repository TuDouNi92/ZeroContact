package net.zerocontact.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.item.backpack.BaseBackpack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public record ToggleBackpackPacket(boolean toggle) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(toggle);
    }

    public static ToggleBackpackPacket decode(FriendlyByteBuf buf) {
        return new ToggleBackpackPacket(buf.readBoolean());
    }

    public static void handle(ToggleBackpackPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                    ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                    if (backpackStack.getItem() instanceof BaseBackpack backpack) {
                        backpack.setToggling(backpackStack, msg.toggle);
                    }
                });
                handler.getStacksHandler("rigs").ifPresent(stacksHandler -> {
                    ItemStack rigsStack = stacksHandler.getStacks().getStackInSlot(0);
                    if (rigsStack.getItem() instanceof BaseBackpack rigs) {
                        rigs.setToggling(rigsStack, msg.toggle);
                    }
                });
            });
        });
    }
}
