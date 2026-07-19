package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import dev.architectury.event.EventResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.compat.FirstAidCompatHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlateEntityHurtEvent {
    public static boolean modifyDamage(LivingEntity lv, DamageSource source, float amount, ItemStack[] hitStacks) {
        ItemStack armorStack;
        ItemStack plateStack = null;
        if (hitStacks.length <= 1) {
            armorStack = hitStacks[0];
        } else {
            armorStack = hitStacks[1];
            plateStack = hitStacks[0];
        }
        HurtPipeLine pipeLine = new HurtPipeLine();
        HurtPipeLine.DamageResult result = pipeLine.process(new HurtPipeLine.DamageContext(lv, source, amount, plateStack, armorStack));
        return pipeLine.execute(result, () -> lv.hurt(result.finalSource(), result.finalAmount()));
    }


    public static float getHurtAmount(LivingEntity lv, DamageSource source, float amount, @Nullable ICombatArmorItem plateProvider, @Nullable ICombatArmorItem armorProvider, int hurtCanHold) {
        float hurtAmount;
        float generateCaliberDamageAmount;
        if (plateProvider != null && armorProvider != null) {
            generateCaliberDamageAmount = CaliberVariantDamageHelper.generateDamageAmount(amount, source, hurtCanHold, plateProvider);
        } else if (armorProvider != null) {
            generateCaliberDamageAmount = CaliberVariantDamageHelper.generateDamageAmount(amount, source, hurtCanHold, armorProvider);
        } else {
            generateCaliberDamageAmount = CaliberVariantDamageHelper.generateDamageAmount(amount, source, hurtCanHold, null);
        }
        if (armorProvider != null && EventUtil.isIncidentAngleValid(lv, source)) {
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
                FirstAidCompatHandler firstAidCompat = FirstAidCompatHandler.create(livingEntity, damageSource);
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                float amount = event.getBaseAmount();
                Optional.of(helmet).ifPresent(stack -> {
                    if (!(stack.getItem() instanceof HelmetInfoProvider && stack.getItem() instanceof ICombatArmorItem entityHurtProvider))
                        return;
                    int protectionClass = stack.getOrCreateTag().getInt("protection_class");
                    float hurtAmount = getHurtAmount(livingEntity, damageSource, amount, null, entityHurtProvider, protectionClass);
                    if (stack.getMaxDamage() - stack.getDamageValue() <= 1) {
                        hurtAmount = getHurtAmount(livingEntity, damageSource, amount, null, null, protectionClass);
                    }
                    eventPre.setBaseAmount(hurtAmount);
                    if (firstAidCompat != null && firstAidCompat.getHeadApplicable()) {
                        eventPre.setHeadshotMultiplier(0.2f);
                    } else {
                        eventPre.setHeadshotMultiplier(1f);
                    }
                });
            }
        });
    }


    public static EventResult entityHurtRegister(LivingEntity lv, DamageSource source, float amount) {
        if (PlateEntityHurtEvent.modifyDamage(lv, source, amount, EventUtil.getHitBodyPartStack(lv, source))) {
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }
}
