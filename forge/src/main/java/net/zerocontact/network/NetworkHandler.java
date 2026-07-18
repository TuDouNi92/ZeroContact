package net.zerocontact.network;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.animation.VisorTracker;
import net.zerocontact.command.CommandManager;
import net.zerocontact.item.backpack.BaseBackpack;
import net.zerocontact.item.block.WorkBenchEntity;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.function.Supplier;

public class NetworkHandler {

    public static class SyncStaminaPacket {
        private final float stamina;
        private final boolean enabled;

        public SyncStaminaPacket(float stamina, boolean enabled) {
            this.stamina = stamina;
            this.enabled = enabled;
        }

        public SyncStaminaPacket(FriendlyByteBuf buf) {
            this.stamina = buf.readFloat();
            this.enabled = buf.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeFloat(stamina);
            buf.writeBoolean(enabled);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> ClientData.setStamina(stamina, enabled));
        }
    }

    public record ToggleStaminaPacket(boolean isEnable) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(isEnable);
        }

        public static ToggleStaminaPacket decode(FriendlyByteBuf buf) {
            return new ToggleStaminaPacket(buf.readBoolean());
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer serverPlayer = context.getSender();
                Optional.ofNullable(serverPlayer).ifPresent(serverPlayer1 -> {
                    CommandManager.CommandSavedData data = CommandManager.CommandSavedData.get(serverPlayer1.serverLevel());
                    data.setStaminaState(isEnable);
                });
            });
        }
    }

    public record FlipVisorPacket() {
        public void encode(FriendlyByteBuf buf) {
        }

        public static FlipVisorPacket decode(FriendlyByteBuf buf) {
            return new FlipVisorPacket();
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
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

    public record ToggleVisorResultPacket(AnimateData.VisorAnimateData visorAnimateData) {
        public void encode(FriendlyByteBuf buf) {
            String animName = "";
            boolean isPlaying = false;
            double animLength = 0;
            if (visorAnimateData != null) {
                animName = visorAnimateData.animationName;
                isPlaying = visorAnimateData.isPlaying;
                animLength = visorAnimateData.animLength;
            }
            buf.writeUtf(animName);
            buf.writeDouble(animLength);
            buf.writeBoolean(isPlaying);
        }

        public static ToggleVisorResultPacket decode(FriendlyByteBuf buf) {
            return new ToggleVisorResultPacket(
                    new AnimateData.VisorAnimateData(
                            buf.readUtf(),
                            buf.readDouble(),
                            buf.readBoolean()
                    )
            );
        }

        public static void handle(ToggleVisorResultPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> VisorTracker.update(msg.visorAnimateData));
        }
    }

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

    public record RightClickingAllyBackpackPacket() {
        public void encode(FriendlyByteBuf ignoredBuf) {
        }

        public static RightClickingAllyBackpackPacket decode(FriendlyByteBuf buf) {
            return new RightClickingAllyBackpackPacket();
        }

        public static void handle(RightClickingAllyBackpackPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer serverPlayer = context.getSender();
                BaseBackpack.whetherOpenAllyScreen(serverPlayer);
            });
        }
    }

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

    public record OpenAmmoSelectorPacket() {
        public void encode(FriendlyByteBuf buf) {
        }

        public static OpenAmmoSelectorPacket decode(FriendlyByteBuf buf) {
            return new OpenAmmoSelectorPacket();
        }

        public static void handle(OpenAmmoSelectorPacket msg, Supplier<NetworkEvent.Context> supplier) {
            ServerAmmoSelector.handleMenu(msg, supplier);
        }
    }

    public record SelectAmmoPacket(ItemStack ammoItem) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeItem(ammoItem);
        }

        public static SelectAmmoPacket decode(FriendlyByteBuf buf) {
            return new SelectAmmoPacket(buf.readItem());
        }

        public static void handle(SelectAmmoPacket msg, Supplier<NetworkEvent.Context> supplier) {
            ServerAmmoSelector.handleSelected(msg, supplier);
        }
    }

    public record ClientAmmoReloadPacket() {
        public void encode(FriendlyByteBuf buf) {
        }

        public static ClientAmmoReloadPacket decode(FriendlyByteBuf buf) {
            return new ClientAmmoReloadPacket();
        }


        public static void handle(ClientAmmoReloadPacket __, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
                if (operator != null) {
                    operator.reload();
                }
            });
        }
    }
}
