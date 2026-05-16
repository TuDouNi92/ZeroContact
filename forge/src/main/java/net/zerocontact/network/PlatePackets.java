package net.zerocontact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PlatePackets {
    public record PlateExtractor(int armorSlotIndex) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(armorSlotIndex);
        }

        public static PlateExtractor decode(FriendlyByteBuf buf) {
            return new PlateExtractor(buf.readInt());
        }

        public static void handle(PlateExtractor msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                ItemStack slotStack = player.getSlot(msg.armorSlotIndex).get();
                @NotNull LazyOptional<IItemHandler> capabilityLazyOptional = slotStack.getCapability(ForgeCapabilities.ITEM_HANDLER);
                capabilityLazyOptional.ifPresent(handler -> {
                    ItemStack extractedItem = handler.extractItem(slotStack.getOrCreateTag().getInt("PointingSlot"), 1, false);
                    if (!extractedItem.isEmpty() && slotStack.getItem() instanceof BaseArmorGeoImpl baseArmorGeo) {
                        player.getInventory().add(extractedItem);
                        baseArmorGeo.serialize(slotStack, (ItemStackHandler) handler);
                        player.level().playSound(null, player.blockPosition(), ModSoundEventsReg.ARMOR_EQUIP_PLATE, SoundSource.PLAYERS);
                    }
                });
            });
        }
    }

    public record PlateInserter(int armorSlotIndex) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(armorSlotIndex);
        }

        public static PlateInserter decode(FriendlyByteBuf buf) {
            return new PlateInserter(buf.readInt());
        }

        public static void handle(PlateInserter msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                ItemStack carrieStack = player.inventoryMenu.getCarried();
                ItemStack slotStack = player.getSlot(msg.armorSlotIndex).get();
                @NotNull LazyOptional<IItemHandler> capabilityLazyOptional = slotStack.getCapability(ForgeCapabilities.ITEM_HANDLER);
                capabilityLazyOptional.ifPresent(handler -> {
                    if (carrieStack.getItem() instanceof PlateInfoProvider && slotStack.getItem() instanceof BaseArmorGeoImpl baseArmorGeo) {
                        int plateSlotIndex = slotStack.getOrCreateTag().getInt("PointingSlot");
                        ItemStack bundleStack = handler.getStackInSlot(plateSlotIndex);
                        if (!bundleStack.isEmpty()) {
                            handler.extractItem(plateSlotIndex,1,false);
                            handler.insertItem(plateSlotIndex, carrieStack, false);
                            player.inventoryMenu.setCarried(bundleStack.copy());
                        } else {
                            player.inventoryMenu.setCarried(handler.insertItem(plateSlotIndex, carrieStack, false));
                        }
                        baseArmorGeo.serialize(slotStack, (ItemStackHandler) handler);
                        player.level().playSound(null, player.blockPosition(), ModSoundEventsReg.ARMOR_EQUIP_PLATE, SoundSource.PLAYERS);
                    }
                });
            });
        }
    }

    public record IndexUpdater(int armorSlotIndex, int plateSlotIndex) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(armorSlotIndex);
            buf.writeInt(plateSlotIndex);
        }

        public static IndexUpdater decode(FriendlyByteBuf buf) {
            return new IndexUpdater(buf.readInt(), buf.readInt());
        }

        public static void handle(IndexUpdater msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                ItemStack slotStack = player.getSlot(msg.armorSlotIndex).get();
                slotStack.getOrCreateTag().putInt("PointingSlot", msg.plateSlotIndex);
            });
        }
    }
}
