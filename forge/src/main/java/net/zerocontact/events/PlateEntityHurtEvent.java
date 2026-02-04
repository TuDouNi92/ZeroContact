package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {
    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, ItemStack plateSlot) {
        EntityKineticBullet.EntityResult result = EventUtil.getHitResult(source);
        boolean isHeadShot = result != null && result.isHeadshot();
        if (lv instanceof ServerPlayer serverPlayer && serverPlayer.isCreative()) return false;
        DamageSource modifiedDamageSource = ModDamageTypes.Sources.bullet(lv.level().registryAccess(), null, null, false);
        AtomicBoolean interruptResult = new AtomicBoolean();
        ItemStack stackInVanillaSlot = lv.getItemBySlot(EquipmentSlot.CHEST);
        if (isHeadShot) return false;
        if (stackInVanillaSlot.getItem() instanceof BaseArmorGeoImpl baseArmorGeo && baseArmorGeo.getArmorType().equals(ArmorTypeTag.ArmorType.ARMOR)) {
            hurtIt(lv, source, amount, stackInVanillaSlot, modifiedDamageSource, interruptResult);
        } else {
            hurtIt(lv, source, amount, plateSlot, modifiedDamageSource, interruptResult);
        }
        return interruptResult.get();
    }

    private static void hurtIt(LivingEntity lv, DamageSource source, float amount, ItemStack stack, DamageSource modifiedDamageSource, AtomicBoolean interruptResult) {
        float hurtAmount;
        int protectionClass = stack.getOrCreateTag().getInt("protection_class");

        //This guard pattern is important to prevent recursively call from event
        if (!stack.isEmpty() && source.getEntity() != null) {
            lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
            if (EventUtil.isDamageSourceValid(source) && stack.getItem() instanceof EntityHurtProvider provider) {
                hurtAmount = getHurtAmount(lv, source, amount, provider, protectionClass);
                lv.hurt(modifiedDamageSource, hurtAmount);
            }
            interruptResult.set(true);
        } else {
            interruptResult.set(false);
        }
    }

    private static float getHurtAmount(LivingEntity lv, DamageSource source, float amount, EntityHurtProvider provider, int hurtCanHold) {
        float hurtAmount;
        float generateCaliberDamageAmount = CaliberVariantDamageHelper.generateDamageAmount(amount, source, hurtCanHold, provider);
        if (EventUtil.isIncidentAngleValid(lv, source)) {
            hurtAmount = provider.generateRicochet() * generateCaliberDamageAmount;
        } else {
            hurtAmount = generateCaliberDamageAmount;
        }
        return hurtAmount;
    }

    public static void entityHurtByGunHeadShot(EntityHurtByGunEvent event) {
        if (!(event instanceof EntityHurtByGunEvent.Pre eventPre)) return;
        boolean isHeadShot = event.isHeadShot();
        if (!isHeadShot) return;
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        DamageSource damageSource = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
        entity.ifPresent(e -> {
            if (e instanceof LivingEntity livingEntity) {
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                float amount = event.getBaseAmount();
                Optional.of(helmet).ifPresent(stack -> {
                    if (!(stack.getItem() instanceof HelmetInfoProvider && stack.getItem() instanceof EntityHurtProvider entityHurtProvider))
                        return;
                    int protectionClass = stack.getOrCreateTag().getInt("protection_class");
                    float hurtAmount = getHurtAmount(livingEntity, damageSource, amount, entityHurtProvider, protectionClass);
                    eventPre.setBaseAmount(hurtAmount);
                    eventPre.setHeadshotMultiplier(1f);
                    playHeadshotSound(livingEntity);
                });
            }
        });
    }

    private static void playHeadshotSound(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ModSoundEventsReg.HELMET_HIT, SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
        livingEntity.playSound(ModSoundEventsReg.HELMET_HIT);
    }

    public static EventResult entityHurtRegister(LivingEntity lv, DamageSource source, float amount) {
        if (PlateEntityHurtEvent.changeHurtAmountRicochet(lv, source, amount, EventUtil.idHitFromBack(lv, source))) {
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }
}
