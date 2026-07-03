package net.zerocontact.events;

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

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BulletBinder {
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
                            if (context == null) return;
                            if (AmmoInjector.isEmptyContext(context)) {
                                AmmoInjector.setEntityGunContext(stack);
                            }
                        });
            }
            //For none-player, simply check handStack
            else {
                ItemStack checkHandStack = livingEntity.getMainHandItem();
                if (checkHandStack.getItem() instanceof AbstractGunItem) {
                    AmmoInjector.AmmoContext context = AmmoInjector.read(checkHandStack);
                    if (context == null) return;
                    if (AmmoInjector.isEmptyContext(context)) {
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
            if (context == null) return;
            AmmoInjector.bind(bullet, context);
        }

        //ClientLevel to operate tracer color
        if (owner == null) {
            AmmoInjector.AmmoContext context = AmmoInjector.get(bullet);
            if (context == null) return;
            bullet.getPersistentData().putIntArray(EntityKineticBullet.TRACER_COLOR_OVERRIDER_KEY, context.caliber().tracerColor());
        }

    }

    @SubscribeEvent
    public static void onBulletDespawn(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity))
            return;
        AmmoInjector.consume(bullet);
    }
}
