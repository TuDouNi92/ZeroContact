package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlateEntityHurtEvent {
    public static boolean isHeadShot;

    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, String identifier) {
        if (lv instanceof ServerPlayer serverPlayer && serverPlayer.isCreative()) return false;
        DamageSource modifiedDamageSource = ModDamageTypes.Sources.bullet(lv.level().registryAccess(), source.getDirectEntity(), source.getEntity(), false);
        AtomicBoolean result = new AtomicBoolean();
        AtomicReference<Float> updatedHurtAmountFromCurio = new AtomicReference<>((float) 0);
        result.set(false);
        if (isHeadShot) return false;
        CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            updatedHurtAmountFromCurio.set(hurtIt(lv, source, amount, stack, modifiedDamageSource, result));
        }));
        ItemStack stackInVanillaSlot = lv.getItemBySlot(EquipmentSlot.CHEST);
        if (stackInVanillaSlot.getItem() instanceof BaseArmorGeoImpl) {
            hurtIt(lv, source, updatedHurtAmountFromCurio.get() == 0 ? amount : updatedHurtAmountFromCurio.get(), stackInVanillaSlot, modifiedDamageSource, result);
        }
        return result.get();
    }

    private static float hurtIt(LivingEntity lv, DamageSource source, float amount, ItemStack stack, DamageSource modifiedDamageSource, AtomicBoolean result) {
        float hurtAmount = 0;
        int hurtCanHold = stack.getOrCreateTag().getInt("absorb");
        if (!stack.isEmpty() && source.type() != modifiedDamageSource.type()) {
            lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
            if (EventUtil.isDamageSourceValid(source) && stack.getItem() instanceof EntityHurtProvider provider) {
                if (EventUtil.isIncidentAngleValid(lv, source)) {
                    hurtAmount = provider.generateRicochet() * amount;
                    lv.hurt(modifiedDamageSource, hurtAmount);
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
                result.set(true);
            } else {
                result.set(false);
            }
        }
        return hurtAmount;
    }

    public static void entityHurtByGunHeadShot(EntityHurtByGunEvent event) {
        if(!(event instanceof EntityHurtByGunEvent.Pre eventPre))return;
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
                    livingEntity.playSound(ModSoundEventsReg.HELMET_HIT);
                });
            }
        });
    }
}
