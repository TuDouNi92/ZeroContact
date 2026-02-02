package net.zerocontact.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.animation.VisorTracker;
import net.zerocontact.command.CommandManager;
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

    public record ToggleVisorPacket(boolean isToggle) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(isToggle);
        }

        public static ToggleVisorPacket decode(FriendlyByteBuf buf) {
            return new ToggleVisorPacket(buf.readBoolean());
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                Optional<ItemStack> helmet = Optional.of(player.getItemBySlot(EquipmentSlot.HEAD));
                helmet.ifPresent(stack -> {
                    if (stack.getItem() instanceof Toggleable toggleable) {
                        boolean isEnabled = toggleable.getEnabled();
                        toggleable.setToggling(true);
                        ModMessages.sendToPlayer(new ToggleVisorResultPacket(isEnabled, toggleable.getAnimData()), player);
                    }
                });
            });
        }
    }

    public record ToggleVisorResultPacket(boolean lastSwitch, AnimateData.VisorAnimateData visorAnimateData) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(lastSwitch);
            String animName = "";
            double tick = 0;
            boolean isPlaying = false;
            double animLength = 0;
            if (visorAnimateData != null) {
                animName = visorAnimateData.animationName;
                tick = visorAnimateData.tick;
                isPlaying = visorAnimateData.isPlaying;
                animLength = visorAnimateData.animLength;
            }
            buf.writeUtf(animName);
            buf.writeDouble(tick);
            buf.writeDouble(animLength);
            buf.writeBoolean(isPlaying);
        }

        public static ToggleVisorResultPacket decode(FriendlyByteBuf buf) {
            return new ToggleVisorResultPacket(
                    buf.readBoolean(),
                    new AnimateData.VisorAnimateData(
                            buf.readUtf(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readBoolean()
                    )
            );
        }

        public static void handle(ToggleVisorResultPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ClientData.setLastToggleVisorEnabled(msg.lastSwitch);
                VisorTracker.update(msg.visorAnimateData);
            });
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
                        if (stacksHandler.getStacks().getStackInSlot(0).getItem() instanceof Toggleable.Backpack backpack) {
                            backpack.setToggling(msg.toggle);
                            boolean switchToggleable = backpack.getToggling();
                            ModMessages.sendToPlayer(new ToggleBackpackResultPacket(switchToggleable), player);
                        }
                    });
                });
            });
        }
    }

    public record ToggleBackpackResultPacket(boolean toggle) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(toggle);
        }

        public static ToggleBackpackResultPacket decode(FriendlyByteBuf buf) {
            return new ToggleBackpackResultPacket(buf.readBoolean());
        }

        public static void handle(ToggleBackpackResultPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> ClientData.setTriggerBackPackToggle(msg.toggle));
        }
    }
}
