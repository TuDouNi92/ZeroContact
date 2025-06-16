package net.zerocontact.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

import java.util.UUID;

public class PlayerStamina {
    private float stamina = 120f;
    private int tickCounter = 0;
    private int cooldownTicks = 0;

    public float getStamina() {
        return stamina;
    }

    public void setStamina(float value) {
        this.stamina = Math.max(0f, Math.min(120f, value));
    }

    public void setNBT(CompoundTag nbt) {
        nbt.putFloat("stamina", stamina);
    }

    public void getNbt(CompoundTag nbt) {
        stamina = nbt.getFloat("stamina");
    }

    public static void interruptSprint(Player player, boolean active) {
        if (player.isCreative()) return;
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        UUID uuid = UUID.nameUUIDFromBytes("StaminaRunOut".getBytes());
        if (speedAttr == null) return;
        if (active) {
            if(speedAttr.getModifier(uuid)==null){
                speedAttr.addTransientModifier(new AttributeModifier(
                        uuid,
                        "StaminaRunOut",
                        -0.05,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
        else{
            speedAttr.removeModifier(uuid);
        }
    }

    public static void staminaTick(Player player) {
        player.getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(playerStamina -> {
            playerStamina.tickCounter++;
            float stamina = playerStamina.getStamina();
            if (player.isSprinting()) {
                playerStamina.cooldownTicks = 60;
                playerStamina.setStamina(stamina - 1.0f);
            }
            if (playerStamina.tickCounter >= 20 && playerStamina.cooldownTicks == 0) {
                playerStamina.setStamina(stamina + 4.0f);
                playerStamina.tickCounter = 0;
            }
            playerStamina.cooldownTicks = Math.max(0, --playerStamina.cooldownTicks);
            ZeroContactLogger.LOG.debug(playerStamina.cooldownTicks);
            if (!player.level().isClientSide) {
                ModMessages.sendToPlayer(new NetworkHandler.SyncStaminaPacket(playerStamina.getStamina()), (ServerPlayer) player);
            }
            interruptSprint(player, stamina==0);
        });
    }
}
