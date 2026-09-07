package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.entity.EntityKineticBullet;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.HelmetInfoProvider;
import net.zerocontact.caliber.*;
import net.zerocontact.caliber.damage.DamagePipeLine;
import net.zerocontact.caliber.damage.DamageProcessor;
import net.zerocontact.caliber.damage.HitUtil;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.caliber.damage.model.DamageResult;
import net.zerocontact.caliber.extension.HookDispatcher;
import net.zerocontact.caliber.extension.HookEventTrigger;
import net.zerocontact.caliber.extension.model.HookContext;
import net.zerocontact.caliber.registry.MobRuleRegistry;
import net.zerocontact.compat.FirstAidCompatHandler;
import net.zerocontact.datagen.model.MobRulesPOJO;

import java.util.Optional;

public class PlateEntityHurtEvent {
    public static boolean modifyDamage(LivingEntity lv, DamageSource source, float amount, ItemStack[] hitStacks) {
        ItemStack armorStack;
        ItemStack plateStack = ItemStack.EMPTY;
        if (hitStacks.length <= 1) {
            armorStack = hitStacks[0];
        } else {
            armorStack = hitStacks[1];
            plateStack = hitStacks[0];
        }
        DamagePipeLine pipeLine = new DamagePipeLine();
        DamageResult result = pipeLine.process(new DamageContext(lv, source, amount, plateStack, armorStack));
        return pipeLine.execute(result, () -> {
            lv.hurt(result.finalSource(), result.finalAmount());
            EntityKineticBullet bullet = (EntityKineticBullet) source.getDirectEntity();
            Entity causingEntity = source.getEntity();
            if (bullet != null && lv.level() instanceof ServerLevel serverLevel && causingEntity instanceof LivingEntity) {
                AmmoInjector.AmmoContext context = BulletBinder.getContext(bullet);
                if (context == null) return;
                HookDispatcher.fire(HookEventTrigger.HIT_ENTITY, new HookContext(serverLevel, (LivingEntity) causingEntity, lv, lv.position(), context.caliber()));
            }
        });
    }


    public static void modifyEventIfHeadshot(EntityHurtByGunEvent event) {
        if (!(event instanceof EntityHurtByGunEvent.Pre eventPre)) return;
        boolean isHeadShot = event.isHeadShot();
        if (!isHeadShot) return;
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        DamageSource damageSource = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
        float amount = event.getBaseAmount();
        entity.ifPresent(e -> {
            if (e instanceof LivingEntity livingEntity) {
                FirstAidCompatHandler firstAidCompat = FirstAidCompatHandler.create(livingEntity, damageSource);
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                if (!(helmet.getItem() instanceof HelmetInfoProvider && helmet.getItem() instanceof ICombatArmorItem entityHurtProvider)) {
                    EntityType<?> type = e.getType();
                    ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(type);
                    MobRulesPOJO.Pattern mobPattern = MobRuleRegistry.get(mobId);

                    if (mobPattern != null) {
                        eventPre.setBaseAmount(amount * Math.max(0,mobPattern.headshotMultiplier()));
                    }
                    return;
                }
                int protectionClass = helmet.getOrCreateTag().getInt("protection_class");
                float hurtAmount = DamageProcessor.getHurtAmount(livingEntity, damageSource, amount, null, entityHurtProvider, protectionClass);
                if (helmet.getMaxDamage() - helmet.getDamageValue() <= 1) {
                    hurtAmount = DamageProcessor.getHurtAmount(livingEntity, damageSource, amount, null, null, protectionClass);
                }
                eventPre.setBaseAmount(hurtAmount);

                if (firstAidCompat != null && firstAidCompat.getHeadApplicable()) {
                    eventPre.setHeadshotMultiplier(0.2f);
                } else {
                    eventPre.setHeadshotMultiplier(1f);
                }
            }
        });
    }


    public static EventResult entityHurtRegister(LivingEntity lv, DamageSource source, float amount) {
        if (PlateEntityHurtEvent.modifyDamage(lv, source, amount, HitUtil.getHitBodyPartStack(lv, source))) {
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }
}
