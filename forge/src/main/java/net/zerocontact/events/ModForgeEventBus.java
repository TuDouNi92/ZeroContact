package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.interaction.BulletPassBy;
import net.zerocontact.client.menu.BackpackContainerMenu;
import net.zerocontact.command.CommandManager;
import net.zerocontact.item.backpack.BaseBackpack;
import net.zerocontact.item.helmet.BaseGeoHelmet;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import net.zerocontact.stamina.PlayerStamina;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEventBus {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientEntity(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet) {
            BulletPassBy.playBulletPassBySound(entity, Minecraft.getInstance().player);
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractBackpack(PlayerInteractEvent.RightClickEmpty event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            whetherOpenAlliesScreen(player);
        }
    }

    private static ServerPlayer getLookAtPlayer(Player player) {
        double range = 1.5D;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(range));
        AABB box = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePos, reachVec, box, e ->
                        e instanceof ServerPlayer sp
                                && !sp.isSpectator()
                                && sp.isPickable()
                                && sp.isAlliedTo(player)
        );
        return result != null ? (ServerPlayer) result.getEntity() : null;
    }

    private static boolean isLookAtTargetBack(ServerPlayer player, @Nullable LivingEntity target) {
        if (target == null) return false;
        Vec3 look = player.getLookAngle().normalize();
        float yRot = target.getYRot();
        Vec3 targetForward = Vec3.directionFromRotation(0, yRot).normalize();
        Vec3 targetBack = targetForward.scale(-1);
        double dot = look.dot(targetBack);
        return dot > 0.7;
    }

    private static void whetherOpenAlliesScreen(ServerPlayer player) {
        ServerPlayer targetEntity = getLookAtPlayer(player);
        if (isLookAtTargetBack(player, targetEntity) && player.isCrouching()) {
            CuriosApi.getCuriosInventory(targetEntity)
                    .ifPresent(itemHandler ->
                            itemHandler.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                                ItemStack backpackStack = stacksHandler.getStacks().getStackInSlot(0);
                                if (backpackStack.getItem() instanceof BaseBackpack backpack)
                                    backpack.callOpenScreen(player, BackpackContainerMenu.TriggerSource.USE);
                            }));
        }
    }

    @SubscribeEvent
    public static void onVisorEquip(LivingEquipmentChangeEvent equipmentChangeEvent) {
        if (!(equipmentChangeEvent.getEntity() instanceof ServerPlayer player)) return;
        ItemStack newGear = equipmentChangeEvent.getTo();
        EquipmentSlot slot = equipmentChangeEvent.getSlot();
        if(slot == EquipmentSlot.HEAD && (newGear.getItem() instanceof Toggleable  helmet && newGear.getItem() instanceof BaseGeoHelmet)){
            Boolean lastToggleVisor = helmet.readStatus(newGear,"VisorOn");
            ModMessages.sendToPlayer(new NetworkHandler.ToggleVisorResultPacket(!lastToggleVisor,helmet.getAnimData()),player);
        }
    }
        @SubscribeEvent
        public static void RegCommands (RegisterCommandsEvent event){
            CommandManager.register(event.getDispatcher());
        }

        public static void regEvents () {
            ModMessages.register();
            TickEvent.PLAYER_PRE.register(PlayerStamina::staminaTick);
            dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register((lv, source, amount) -> {
                        PlateDamageEvent.DamagePlateRegister(lv, source, amount);
                        return PlateEntityHurtEvent.entityHurtRegister(lv, source, amount);
                    }
            );
        }

        @SubscribeEvent
        public static void entityHurtByGunEvent (EntityHurtByGunEvent event){
            PlateEntityHurtEvent.entityHurtByGunHeadShot(event);
            PlateDamageEvent.DamageHelmet(event);
        }
    }
