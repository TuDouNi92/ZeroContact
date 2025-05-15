package net.zerocontact.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.zerocontact.ModLogger;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

public class PlayerStamina {
    private float stamina = 120f;
    private  int tickCounter = 0;
    private  int cooldownTicks = 0;

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

    public static void staminaTick(Player player) {
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(playerStamina -> {
            playerStamina.tickCounter++;
            float stamina = playerStamina.getStamina();
            if(playerStamina.cooldownTicks!=0){
                player.setSprinting(false);
            }
            if (player.isSprinting()) {
                if (stamina < 1 && playerStamina.cooldownTicks == 0) {
                    playerStamina.cooldownTicks = 60;
                }
                playerStamina.setStamina(stamina - 1.0f);
                if (playerStamina.tickCounter >= 10) {
                    playerStamina.tickCounter = 0;
                }
            } else {
                if (playerStamina.tickCounter >= 10 && playerStamina.cooldownTicks == 0) {
                    playerStamina.setStamina(stamina + 8.0f);
                    playerStamina.tickCounter = 0;
                }
            }
            playerStamina.cooldownTicks = Math.max(0, --playerStamina.cooldownTicks);
            ModLogger.LOG.debug(playerStamina.cooldownTicks);
            ModMessages.sendToPlayer(new NetworkHandler.SyncStaminaPacket(playerStamina.getStamina()), (ServerPlayer) player);
        });

    }
}
