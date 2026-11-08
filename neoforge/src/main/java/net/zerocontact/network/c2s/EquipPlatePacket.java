package net.zerocontact.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.item.plate.BasePlate;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.function.Supplier;

public record EquipPlatePacket(
        ItemStack snapShot
) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeItemStack(snapShot, false);
    }

    public static EquipPlatePacket decode(FriendlyByteBuf buf) {
        return new EquipPlatePacket(buf.readItem());
    }

    public static void handle(EquipPlatePacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ItemStack handStack = player.getMainHandItem();
            if (!ItemStack.isSameItemSameTags(handStack, msg.snapShot)) return;
            BasePlate.resolveSlot(player, (itemHandler, front, back) -> {
                ItemStack frontPlate = front.map(s -> s.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                ItemStack backPlate = back.map(s -> s.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                if (frontPlate.isEmpty()) {
                    itemHandler.setEquippedCurio(CuriosConstants.FRONT_PLATE,0,handStack.copy());
                    handStack.shrink(1);
                    player.playNotifySound(ModSoundEventsReg.ARMOR_EQUIP_PLATE, SoundSource.PLAYERS,1.0f,1.0f);
                } else if (backPlate.isEmpty()) {
                    itemHandler.setEquippedCurio(CuriosConstants.BACK_PLATE,0,handStack.copy());
                    handStack.shrink(1);
                    player.playNotifySound(ModSoundEventsReg.ARMOR_EQUIP_PLATE, SoundSource.PLAYERS,1.0f,1.0f);
                }
            });
        });
        context.setPacketHandled(true);
    }
}
