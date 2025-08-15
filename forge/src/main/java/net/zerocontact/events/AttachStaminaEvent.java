package net.zerocontact.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.command.CommandManager;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import net.zerocontact.stamina.PlayerStamina;
import net.zerocontact.stamina.PlayerStaminaProvider;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class AttachStaminaEvent {
    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!event.getObject().getCapability(PlayerStaminaProvider.PLAYER_STAMINA).isPresent()) {
                event.addCapability(new ResourceLocation(MOD_ID, "properties"), new PlayerStaminaProvider(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone player) {
        if (player.isWasDeath()) {
            player.getOriginal().getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(oldStore -> {
                player.getEntity().getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(newStore -> {
                    newStore.setStamina(oldStore.getStamina());
                });
            });
        }
    }

    @SubscribeEvent
    public static void onRegCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerStamina.class);
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide) {
            if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
                player.getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(playerStamina -> {
                    ModMessages.sendToPlayer(new NetworkHandler.SyncStaminaPacket(playerStamina.getStamina(), CommandManager.CommandSavedData.get(serverLevel).staminaState), player);
                });
            }
        }
    }
}
