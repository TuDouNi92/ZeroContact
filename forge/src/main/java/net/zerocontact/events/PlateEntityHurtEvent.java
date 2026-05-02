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
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {
    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, ItemStack plateSlot) {
        EntityKineticBullet.EntityResult result = EventUtil.getHitResult(source);
        boolean isHeadShot = result != null && result.isHeadshot();
        if (lv instanceof ServerPlayer serverPlayer && serverPlayer.isCreative()) return false;
        DamageSource modifiedDamageSource = ZDamageTypes.create(lv.level());
        AtomicBoolean interruptResult = new AtomicBoolean();
        ItemStack armorSlot = lv.getItemBySlot(EquipmentSlot.CHEST);
        if (isHeadShot) return false;
        if (armorSlot.getItem() instanceof BaseArmorGeoImpl baseArmorGeo && baseArmorGeo.getArmorType().equals(IEquipmentTypeTag.EquipmentType.ARMOR)) {
            hurtIt(lv, source, amount, null, armorSlot, modifiedDamageSource, interruptResult);
        } else {
            hurtIt(lv, source, amount, plateSlot, armorSlot, modifiedDamageSource, interruptResult);
        }
        return interruptResult.get();
    }

    private static void hurtIt(LivingEntity lv, DamageSource source, float amount, @Nullable ItemStack plateStack, ItemStack armorStack, DamageSource modifiedDamageSource, AtomicBoolean interruptResult) {
        float hurtAmount;
        if (plateStack != null && plateStack.getItem() instanceof ICombatArmorItem) {
            int plateProtectionClass = plateStack.getOrCreateTag().getInt("protection_class");
            if (source.is(ModDamageTypes.BULLET)) {
                lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
                if (EventUtil.isDamageSourceValid(source)) {
                    hurtAmount = getHurtAmount(lv, source, amount, (ICombatArmorItem) plateStack.getItem(), (ICombatArmorItem) armorStack.getItem(), plateProtectionClass);
                    lv.hurt(modifiedDamageSource, hurtAmount);
                }
                interruptResult.set(true);
            }
            else{
                interruptResult.set(false);
            }
        } else {
            int protectionClass = armorStack.getOrCreateTag().getInt("protection_class");
            if (!armorStack.isEmpty() && source.is(ModDamageTypes.BULLET)) {
                lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
                if (EventUtil.isDamageSourceValid(source) && armorStack.getItem() instanceof ICombatArmorItem provider) {
                    hurtAmount = getHurtAmount(lv, source, amount, null, provider, protectionClass);
                    lv.hurt(modifiedDamageSource, hurtAmount);
                }
                interruptResult.set(true);
            } else {
                interruptResult.set(false);
            }
        }
    }

    private static float getHurtAmount(LivingEntity lv, DamageSource source, float amount, @Nullable ICombatArmorItem plateProvider, ICombatArmorItem armorProvider, int hurtCanHold) {
        float hurtAmount;
        float generateCaliberDamageAmount;
        if (plateProvider != null) {
            generateCaliberDamageAmount = CaliberVariantDamageHelper.generateDamageAmount(amount, source, hurtCanHold, plateProvider);
            generateCaliberDamageAmount *= armorProvider.generatePenetrated();
        } else {
            generateCaliberDamageAmount = CaliberVariantDamageHelper.generateDamageAmount(amount, source, hurtCanHold, armorProvider);
        }
        if (EventUtil.isIncidentAngleValid(lv, source)) {
            hurtAmount = armorProvider.generateRicochet() * generateCaliberDamageAmount;
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
                    if (!(stack.getItem() instanceof HelmetInfoProvider && stack.getItem() instanceof ICombatArmorItem entityHurtProvider))
                        return;
                    int protectionClass = stack.getOrCreateTag().getInt("protection_class");
                    float hurtAmount = getHurtAmount(livingEntity, damageSource, amount, null, entityHurtProvider, protectionClass);
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
