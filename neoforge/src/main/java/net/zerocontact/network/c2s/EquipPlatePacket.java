package net.zerocontact.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.item.plate.BasePlate;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.Optional;

public record EquipPlatePacket(
        ItemStack snapShot
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipPlatePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(net.zerocontact.ZeroContact.MOD_ID, "equip_plate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipPlatePacket> STREAM_CODEC =
            StreamCodec.ofMember(EquipPlatePacket::encode, EquipPlatePacket::decode);

    @Override
    public CustomPacketPayload.Type<EquipPlatePacket> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, snapShot);
    }

    public static EquipPlatePacket decode(RegistryFriendlyByteBuf buf) {
        return new EquipPlatePacket(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    public static void handle(EquipPlatePacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
            if (player == null || !player.isAlive() || player.isRemoved() || player.isSpectator()) return;
            ItemStack handStack = player.getMainHandItem();
            if (!(handStack.getItem() instanceof BasePlate)) return;
            if (!ItemStack.isSameItemSameComponents(handStack, msg.snapShot)) return;
            BasePlate.resolveSlot(player, (itemHandler, front, back) -> {
                Optional<ItemStack> frontPlate = front.map(s -> s.getStacks().getStackInSlot(0));
                Optional<ItemStack> backPlate = back.map(s -> s.getStacks().getStackInSlot(0));

                if (frontPlate.isPresent() && backPlate.isPresent()) {
                    if (frontPlate.get().isEmpty()) {
                        itemHandler.setEquippedCurio(CuriosConstants.FRONT_PLATE, 0, handStack.copy());
                        handStack.shrink(1);
                        player.playNotifySound(ModSoundEventsReg.ARMOR_EQUIP_PLATE, SoundSource.PLAYERS, 1.0f, 1.0f);

                    }
                    if (backPlate.get().isEmpty()) {
                        itemHandler.setEquippedCurio(CuriosConstants.BACK_PLATE, 0, handStack.copy());
                        handStack.shrink(1);
                        player.playNotifySound(ModSoundEventsReg.ARMOR_EQUIP_PLATE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    }
                }
            });
        });
    }
}
