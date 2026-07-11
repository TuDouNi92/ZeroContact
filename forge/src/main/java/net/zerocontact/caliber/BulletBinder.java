package net.zerocontact.caliber;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BulletBinder {
    private static final Map<UUID, AmmoInjector.AmmoContext> mapping = new HashMap<>();

    //Bind in spawn
    public static void bind(EntityKineticBullet bullet, AmmoInjector.AmmoContext context) {
        mapping.put(bullet.getUUID(), context);
    }

    public static @Nullable AmmoInjector.AmmoContext getContext(EntityKineticBullet bullet) {
        return mapping.get(bullet.getUUID());
    }

    //Consume in leave event
    public static void consumeBullet(EntityKineticBullet bullet) {
        mapping.remove(bullet.getUUID());
    }


    @SubscribeEvent
    public static void handleEntityBulletSpawn(EntityJoinLevelEvent event) {
        //Bind bullet context for bullet projectile
        if (event.getEntity() instanceof EntityKineticBullet bullet) {
            bindBulletContext(bullet, bullet.getOwner());
        }
        //Initialize gun context for entity joined the world
        else if (event.getEntity() instanceof LivingEntity livingEntity) {

            //Initialize gun context for player
            if (livingEntity instanceof Player player) {
                //Ensure every gun has a valid context;
                player.getInventory().items.stream()
                        .filter(itemStack -> itemStack.getItem() instanceof AbstractGunItem)
                        .forEach(stack -> {
                            AmmoInjector.AmmoContext context = AmmoInjector.read(stack);
                            if (context.isEmpty()) {
                                AmmoInjector.setEntityGunContext(stack);
                            }
                        });
            }
            //For none-player, simply check handStack
            else {
                ItemStack checkHandStack = livingEntity.getMainHandItem();
                if (checkHandStack.getItem() instanceof AbstractGunItem) {
                    AmmoInjector.AmmoContext context = AmmoInjector.read(checkHandStack);
                    if (context.isEmpty()) {
                        AmmoInjector.setEntityGunContext(checkHandStack);
                    }
                }
            }
        }

    }

    private static void bindBulletContext(EntityKineticBullet bullet, Entity owner) {
        if (owner instanceof LivingEntity livingEntity) {
            ItemStack checkHandStack = livingEntity.getMainHandItem();
            if (!(checkHandStack.getItem() instanceof AbstractGunItem)) return;
            AmmoInjector.AmmoContext context = AmmoInjector.read(checkHandStack);
            bind(bullet, context);
        }

        //ClientLevel to operate tracer color
        if (owner == null) {
            AmmoInjector.AmmoContext context = getContext(bullet);
            if (context == null) return;
            bullet.getPersistentData().putIntArray(EntityKineticBullet.TRACER_COLOR_OVERRIDER_KEY, context.caliber().tracerColor());
        }

    }

    @SubscribeEvent
    public static void onBulletDespawn(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity))
            return;
        consumeBullet(bullet);
    }
}
