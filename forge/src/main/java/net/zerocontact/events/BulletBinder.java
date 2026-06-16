package net.zerocontact.events;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BulletBinder {
    @SubscribeEvent
    public static void handleEntityBulletSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity owner) {
            bindBulletContext(bullet, owner);
        } else if (event.getEntity() instanceof LivingEntity livingEntity) {
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

    private static void bindBulletContext(EntityKineticBullet bullet, LivingEntity owner) {
        ItemStack checkHandStack = owner.getMainHandItem();
        if (!(checkHandStack.getItem() instanceof AbstractGunItem)) return;
        AmmoInjector.AmmoContext context = AmmoInjector.read(checkHandStack);
        if (context == null) return;
        AmmoInjector.bind(bullet, context);
    }

    @SubscribeEvent
    public static void onBulletDespawn(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity))
            return;
        AmmoInjector.consume(bullet);
    }
}
