package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.HelmetInfoProvider;
import net.zerocontact.caliber.damage.DamagePipeLine;
import net.zerocontact.caliber.damage.DamageProcessor;
import net.zerocontact.caliber.damage.HitUtil;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.caliber.damage.model.DamageResult;
import net.zerocontact.caliber.registry.MobRuleRegistry;
import net.zerocontact.datagen.model.MobRulesPOJO;

public class PlateEntityHurtEvent {
    public static void modifyDamage(EntityHurtByGunEvent.Pre entityHurtByGunEvent) {
        if (entityHurtByGunEvent.isCanceled() || entityHurtByGunEvent.getLogicalSide().isClient()) return;
        DamageSource source = entityHurtByGunEvent.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
        Entity hurtEntity = entityHurtByGunEvent.getHurtEntity();
        if (!(hurtEntity instanceof LivingEntity livingHurt)) return;
        ResolveHitBodyPartEvent.HitPart hitPart = HitUtil.resolveHitPart(
                livingHurt, source, entityHurtByGunEvent.isHeadShot());
        ItemStack[] hitStacks = HitUtil.getHitBodyPartStack(livingHurt, source, hitPart);
        ItemStack armorStack;
        ItemStack plateStack = ItemStack.EMPTY;
        if (hitStacks.length <= 1) {
            armorStack = hitStacks[0];
        } else {
            armorStack = hitStacks[1];
            plateStack = hitStacks[0];
        }
        DamagePipeLine pipeLine = new DamagePipeLine();
        DamageResult result = pipeLine.process(new DamageContext(entityHurtByGunEvent, plateStack, armorStack, hitPart));
        pipeLine.applyToEvent(result, entityHurtByGunEvent);
    }


    public static DamageProcessor.DamageCalcCtx modifyEventIfHeadshot(
            EntityHurtByGunEvent.Pre eventPre, DamageProcessor.DamageCalcCtx fallback) {
        if (!eventPre.isHeadShot() || !(eventPre.getHurtEntity() instanceof LivingEntity livingEntity)) {
            return fallback;
        }
        DamageSource damageSource = eventPre.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
        float amount = eventPre.getBaseAmount();
        ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof HelmetInfoProvider
                && helmet.getItem() instanceof ICombatArmorItem armorProvider)) {
            ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(livingEntity.getType());
            MobRulesPOJO.Pattern mobPattern = MobRuleRegistry.get(mobId);
            if (mobPattern != null) {
                eventPre.setBaseAmount(amount * Math.max(0, mobPattern.headshotMultiplier()));
            }
            return fallback.withOutputDamage(eventPre.getBaseAmount());
        }

        boolean broken = helmet.getMaxDamage() - helmet.getDamageValue() <= 1;
        DamageProcessor.DamageCalcCtx calculation = DamageProcessor.getHurtAmount(
                livingEntity, damageSource, amount, null, broken ? null : armorProvider,
                broken ? 0 : armorProvider.getAbsorb());
        eventPre.setBaseAmount(calculation.outputDamage());

        // The resolver supplies any compatibility factor; apply it once in the pipeline.
        eventPre.setHeadshotMultiplier(1f);
        return calculation;
    }
}
