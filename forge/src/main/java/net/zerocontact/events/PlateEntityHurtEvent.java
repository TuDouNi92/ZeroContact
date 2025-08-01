package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
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
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {
    public static boolean isHeadShot;
    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, String identifier) {
        if (lv instanceof ServerPlayer serverPlayer && serverPlayer.isCreative()) return false;
        DamageSource modifiedDamageSource = ModDamageTypes.Sources.bullet(lv.level().registryAccess(), null, null, false);
        AtomicBoolean interruptResult = new AtomicBoolean();
        ItemStack stackInVanillaSlot = lv.getItemBySlot(EquipmentSlot.CHEST);
        if (isHeadShot) return false;
        if (stackInVanillaSlot.getItem() instanceof BaseArmorGeoImpl baseArmorGeo && baseArmorGeo.getArmorType().equals(ArmorTypeTag.ArmorType.ARMOR)) {
            hurtIt(lv, source, amount, stackInVanillaSlot, modifiedDamageSource, interruptResult);
        } else {
            CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
                hurtIt(lv, source, amount, stack, modifiedDamageSource, interruptResult);
            }));
        }
        return interruptResult.get();
    }

    private static void hurtIt(LivingEntity lv, DamageSource source, float amount, ItemStack stack, DamageSource modifiedDamageSource, AtomicBoolean interruptResult) {
        float hurtAmount;
        int hurtCanHold = stack.getOrCreateTag().getInt("absorb");

        //This guard pattern is important to prevent recursively call from event
        if (!stack.isEmpty() && source.getEntity() != null) {
            lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
            if (EventUtil.isDamageSourceValid(source) && stack.getItem() instanceof EntityHurtProvider provider) {
                if (EventUtil.isIncidentAngleValid(lv, source)) {
                    hurtAmount = provider.generateRicochet() * amount;
                } else {
                    if (hurtCanHold >= amount) {
                        //钝伤
                        hurtAmount = provider.generateBlunt() * amount;

                    } else {
                        //贯穿
                        hurtAmount = provider.generatePenetrated() * amount;
                    }
                }
                lv.hurt(modifiedDamageSource, hurtAmount);
            }
            interruptResult.set(true);
        }
        else{
            interruptResult.set(false);
        }
    }

    public static void entityHurtByGunHeadShot(EntityHurtByGunEvent event) {
        if (!(event instanceof EntityHurtByGunEvent.Pre eventPre)) return;
        isHeadShot = event.isHeadShot();
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
                    float hurtAmount;
                    int absorb = stack.getOrCreateTag().getInt("absorb");
                    if (EventUtil.isIncidentAngleValid(livingEntity, damageSource)) {
                        hurtAmount = entityHurtProvider.generateRicochet() * amount;
                    } else if (absorb >= amount) {
                        hurtAmount = entityHurtProvider.generateBlunt() * amount;
                    } else {
                        hurtAmount = entityHurtProvider.generatePenetrated() * amount;
                    }
                    eventPre.setBaseAmount(hurtAmount);
                    eventPre.setHeadshotMultiplier(1.25f);
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
