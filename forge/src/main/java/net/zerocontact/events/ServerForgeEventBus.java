package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.Toggleable;
import net.zerocontact.command.CommandManager;
import net.zerocontact.item.helmet.BaseGeoHelmet;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import net.zerocontact.stamina.PlayerStamina;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerForgeEventBus {
    @SubscribeEvent
    public static void onPlayerInteractBackpack(PlayerInteractEvent.EntityInteract event) {
        if(!event.getLevel().isClientSide)return;
        if (event.getTarget().blockPosition().distManhattan(event.getEntity().blockPosition()) < 3.0f) {
            ModMessages.sendToServer(new NetworkHandler.RightClickingAllyBackpackPacket());
        }
    }


    @SubscribeEvent
    public static void onVisorEquip(LivingEquipmentChangeEvent equipmentChangeEvent) {
        if (!(equipmentChangeEvent.getEntity() instanceof ServerPlayer player)) return;
        ItemStack newGear = equipmentChangeEvent.getTo();
        EquipmentSlot slot = equipmentChangeEvent.getSlot();
        if (slot == EquipmentSlot.HEAD && (newGear.getItem() instanceof Toggleable helmet && newGear.getItem() instanceof BaseGeoHelmet)) {
            Boolean lastToggleVisor = helmet.readStatus(newGear, "VisorOn");
            ModMessages.sendToPlayer(new NetworkHandler.ToggleVisorResultPacket(!lastToggleVisor, helmet.getAnimData()), player);
        }
    }

    @SubscribeEvent
    public static void RegCommands(RegisterCommandsEvent event) {
        CommandManager.register(event.getDispatcher());
    }

    public static void regEvents() {
        ModMessages.register();
        TickEvent.PLAYER_PRE.register(PlayerStamina::staminaTick);
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register((lv, source, amount) -> {
                    PlateDamageEvent.DamagePlateRegister(lv, source, amount);
                    return PlateEntityHurtEvent.entityHurtRegister(lv, source, amount);
                }
        );
    }

    @SubscribeEvent
    public static void entityHurtByGunEvent(EntityHurtByGunEvent event) {
        PlateEntityHurtEvent.entityHurtByGunHeadShot(event);
        PlateDamageEvent.DamageHelmet(event);
    }
}
