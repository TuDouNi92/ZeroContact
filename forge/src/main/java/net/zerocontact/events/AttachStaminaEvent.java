package net.zerocontact.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.stamina.PlayerStamina;
import net.zerocontact.stamina.PlayerStaminaProvider;

import static net.zerocontact.ZeroContact.MOD_ID;
@Mod.EventBusSubscriber(modid = MOD_ID)
public class AttachStaminaEvent {
    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof Player){
            if(!event.getObject().getCapability(PlayerStaminaProvider.PLAYER_STAMINA).isPresent()){
                event.addCapability(new ResourceLocation(MOD_ID,"properties"), new PlayerStaminaProvider());
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
        if(event.isWasDeath()){
            event.getOriginal().getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(oldStore->{
                event.getOriginal().getCapability(PlayerStaminaProvider.PLAYER_STAMINA).ifPresent(newStore->{
                    newStore.setStamina(oldStore.getStamina());
                });
            });
        }
    }
    @SubscribeEvent
    public static void onRegCapabilities(RegisterCapabilitiesEvent event){
        event.register(PlayerStamina.class);
    }
}
