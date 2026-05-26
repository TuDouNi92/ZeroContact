package net.zerocontact.events;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BulletBinder {
    @SubscribeEvent
    public static void onBulletSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity owner))
            return;
        Item item = owner.getMainHandItem().getItem();
        if (!(item instanceof AbstractGunItem)) return;
        ItemStack gunStack = owner.getMainHandItem();
        AmmoInjector.AmmoContext context = AmmoInjector.read(gunStack);
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
