package net.zerocontact.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerStamina {
    private float stamina = 240f;
    private static int tickCounter = 0;
    private static int cooldownTicks = 0;
    public float getStamina() {
        return stamina;
    }

    public void setStamina(float value) {
        this.stamina = Math.max(0f, Math.min(120f,value));
    }

    public void setNBT(CompoundTag nbt) {
        nbt.putFloat("stamina", stamina);
    }

    public void getNbt(CompoundTag nbt) {
        stamina = nbt.getFloat("stamina");
    }

    public static void staminaTick(Player player) {
        if(player.level().isClientSide)return;
        tickCounter++;
        player.getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(playerStamina -> {
            float stamina = playerStamina.getStamina();
            if(player.isSprinting()){
                playerStamina.setStamina(stamina-1.0f);
                if(tickCounter>=10){
                    tickCounter =0;
                    player.sendSystemMessage(Component.literal("Subtract stamina"+stamina));
                }
                if(stamina <1 &&cooldownTicks ==0){
                    cooldownTicks =20;
                    player.setSprinting(false);
                }
                else{
                    if(cooldownTicks>0)cooldownTicks--;
                    if(cooldownTicks ==0){
                        player.setSprinting(true);
                    }
                }

            }
            else{
                if(tickCounter>=10){
                    playerStamina.setStamina(stamina + 8.0f);
                    player.sendSystemMessage(Component.literal("Recover stamina"+stamina));
                    tickCounter=0;
                }
            }
        });

    }
}
