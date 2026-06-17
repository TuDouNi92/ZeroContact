package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.registries.ModSoundEventsReg;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlateEntityHurtSoundEvent {
    @SubscribeEvent
    public static void onHurtSound(LivingHurtEvent hurtEvent) {
        DamageSource source = hurtEvent.getSource();
        LivingEntity hurtEntity = hurtEvent.getEntity();
        ItemStack checkHelmetStack = hurtEntity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack checkArmorStack = hurtEntity.getItemBySlot(EquipmentSlot.CHEST);
        EntityKineticBullet.EntityResult hitResult = EventUtil.getHitResult(source);
        Level level = hurtEntity.level();
        if (!(source.is(ModDamageTypes.BULLETS_TAG) || source.is(ZDamageTypes.ZC_DAMAGE))) return;
        playSoundByPart(checkArmorStack, hitResult, level, hurtEntity, checkHelmetStack);
    }

    private static void playSoundByPart(ItemStack checkArmorStack, EntityKineticBullet.EntityResult hitResult, Level level, LivingEntity hurtEntity, ItemStack checkHelmetStack) {
        if (checkArmorStack.getItem() instanceof ICombatArmorItem) {
            if (hitResult != null && !hitResult.isHeadshot()) {
                playHitSound(level, hurtEntity);
            }
        }
        if (checkHelmetStack.getItem() instanceof ICombatArmorItem) {
            if (hitResult != null && hitResult.isHeadshot()) {
                playHeadShotSound(level, hurtEntity);
            }
        }
    }


    private static void playHeadShotSound(Level level, LivingEntity hurtEntity) {
        if (hurtEntity instanceof Player player) {
            level.playSound(player, hurtEntity.blockPosition(), ModSoundEventsReg.HELMET_HIT, SoundSource.PLAYERS);
            player.playNotifySound(ModSoundEventsReg.HELMET_HIT, SoundSource.PLAYERS, 2, 1);
        } else {
            hurtEntity.playSound(ModSoundEventsReg.HELMET_HIT);
        }
    }

    private static void playHitSound(Level level, LivingEntity hurtEntity) {
        if (hurtEntity instanceof Player player) {
            level.playSound(player, hurtEntity.blockPosition(), ModSoundEventsReg.ARMOR_HIT_PLATE, SoundSource.PLAYERS);
            player.playNotifySound(ModSoundEventsReg.ARMOR_HIT_PLATE, SoundSource.PLAYERS, 4, 2);
        } else {
            hurtEntity.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
        }
    }

}
