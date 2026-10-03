package net.zerocontact.caliber.damage;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.HelmetInfoProvider;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.caliber.damage.model.DamageResult;
import net.zerocontact.caliber.registry.MobRuleRegistry;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.datagen.model.MobRulesPOJO;
import net.zerocontact.events.PlateEntityHurtEvent;
import net.zerocontact.events.HitProcessEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPartEnum;

import java.util.ArrayList;
import java.util.List;

import static net.zerocontact.caliber.damage.DamageProcessor.getHurtAmount;

public class DamagePipeLine {
    private final List<DamageModifier> plugins = new ArrayList<>();

    public DamagePipeLine() {
        plugins.addAll(List.of(
                new Modifiers.PlayerFilter(),
                new Modifiers.HeadShotProvider(),
                new Modifiers.DamageAmountModifier(),
                new Modifiers.MobRule(),
                new Modifiers.HitPartDamageFactor(),
                new Modifiers.DamageSourceModifier()
        ));
    }

    public DamageResult process(DamageContext context) {
        DamageResultBuilder currentResult = DamageResultBuilder.create().fromContext(context);
        for (DamageModifier plugin : plugins) {
            currentResult = plugin.apply(context, currentResult);
            if (currentResult.stopExecute) break;
        }
        return currentResult.build();
    }


    public interface DamageModifier {
        DamageResultBuilder apply(DamageContext context, DamageResultBuilder current);
    }

    public static class Modifiers {
        public static class PlayerFilter implements DamageModifier {

            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                DamageResultBuilder builder = current;
                if (context.event().getHurtEntity() instanceof ServerPlayer player && player.isCreative()) {
                    builder = current.stopExecute(true);
                }
                return builder;
            }
        }

        public static class HeadShotProvider implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                boolean headshot = context.hitPart().hitPart() == HitPartEnum.HEAD;
                context.event().setHeadshot(headshot);
                return current.withHeadshot(headshot);
            }
        }

        public static class DamageSourceModifier implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                var source = context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
                if (context.event().getHurtEntity() != null) {
                    source = ZDamageTypes.create(
                            current.build(),
                            context.event().getHurtEntity().level(),
                            context.event().getBullet(),
                            context.event().getAttacker(),
                            context.event().getBullet().position(),
                            false
                    );
                }
                return current.finalSource(source);
            }
        }


        public static class DamageAmountModifier implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                ItemStack armor = context.armor();
                ItemStack plate = context.plate();
                DamageProcessor.DamageCalcCtx calculation =
                        DamageProcessor.DamageCalcCtx.unprocessed(context.event().getBaseAmount());
                DamageResultBuilder builder = current;

                // Limb hits do not pass through a helmet, plate or chest armor.
                if (context.hitPart().isLimb()) {
                    return builder.fromCalculation(getHurtAmount(
                                    context.event().getHurtEntity(),
                                    context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING),
                                    context.event().getBaseAmount(), null, null, 0))
                            .shouldReplaceDamage(true);
                }

                //Generate damage for unarmored entity
                if (ModConfigs.SERVER.enableUniversalFleshDamage().get()) {
                    calculation = getHurtAmount(context.event().getHurtEntity(), context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), context.event().getBaseAmount(), null, null, 0);
                    builder = builder.shouldReplaceDamage(true);
                }

                if (context.event().isHeadShot()) {
                    context.event().setBaseAmount(calculation.outputDamage());
                    calculation = PlateEntityHurtEvent.modifyEventIfHeadshot(context.event(), calculation);
                    int protectionLevel = armor.getItem() instanceof HelmetInfoProvider
                            && armor.getItem() instanceof ICombatArmorItem armorProvider
                            && armor.getMaxDamage() - armor.getDamageValue() > 1
                            ? armorProvider.getAbsorb() : 0;
                    return builder
                            .withHeadshot(true)
                            .setArmorContext(new HitProcessEvent.EventArmorContext(armor, plate, protectionLevel, 0))
                            .fromCalculation(calculation)
                            .shouldReplaceDamage(true)
                            .finalAmount(context.event().getBaseAmount() * context.event().getHeadshotMultiplier());
                }


                if (!armor.isEmpty() || !plate.isEmpty()) {
                    //Generate damage for plate-carrier
                    if (!armor.isEmpty() && !plate.isEmpty()) {
                        if (armor.getItem() instanceof ICombatArmorItem armorProvider && plate.getItem() instanceof ICombatArmorItem plateProvider) {
                            int protectionLevel = plateProvider.getAbsorb();
                            builder = builder.setArmorContext(new HitProcessEvent.EventArmorContext(
                                    armor, plate, 0, protectionLevel));
                            calculation = getHurtAmount(context.event().getHurtEntity(), context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), context.event().getBaseAmount(), plateProvider, armorProvider, protectionLevel);
                            if (armor.getMaxDamage() - armor.getDamageValue() <= 1) {
                                return builder
                                        .fromCalculation(calculation)
                                        .shouldReplaceDamage(true)
                                        .finalAmount(calculation.outputDamage() * (1 + armorProvider.generateBlunt()));
                            }
                        }
                    }
                    //Generate damage for body armor/helmet
                    else if (!armor.isEmpty()) {

                        if (armor.getItem() instanceof ICombatArmorItem armorProvider) {
                            boolean broken = armor.getMaxDamage() - armor.getDamageValue() <= 1;
                            int protectionLevel = broken ? 0 : armorProvider.getAbsorb();
                            builder = builder.setArmorContext(new HitProcessEvent.EventArmorContext(
                                    armor, plate, protectionLevel, 0));
                            calculation = getHurtAmount(context.event().getHurtEntity(), context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), context.event().getBaseAmount(), null, armorProvider, protectionLevel);
                            if (broken) {
                                return builder
                                        .fromCalculation(calculation)
                                        .shouldReplaceDamage(true)
                                        .finalAmount(calculation.outputDamage());
                            }
                        }
                    }
                    //Illegal state, only plates equipped
                    else {
                        calculation = getHurtAmount(context.event().getHurtEntity(), context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), context.event().getBaseAmount(), null, null, 0);
                    }
                    builder = builder.shouldReplaceDamage(true);
                }

                return builder.fromCalculation(calculation);
            }
        }

        public static class MobRule implements DamageModifier {

            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                Entity target = context.event().getHurtEntity();
                if (target == null) return current;
                EntityType<?> type = target.getType();
                ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(type);
                MobRulesPOJO.Pattern mobPattern = MobRuleRegistry.get(mobId);
                if (mobPattern == null || context.event().isHeadShot()) return current;
                return current.finalAmount(
                        current.finalAmount * Math.max(0, mobPattern.bodyshotMultiplier())
                );
            }
        }

        public static class HitPartDamageFactor implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                float factor = context.hitPart().damageFactor();
                if (factor == 1f) return current;
                return current.shouldReplaceDamage(true).finalAmount(current.finalAmount * factor);
            }
        }

    }

    public void applyToEvent(DamageResult result, EntityHurtByGunEvent.Pre event) {
        if (result.shouldStopExecute() || !result.shouldReplaceDamage()) return;

        HitProcessEvent.Pre pre = new HitProcessEvent.Pre(
                event.getAttacker(),
                event.getHurtEntity(),
                event.getBullet(),
                result.finalSource(),
                result.armorContext(),
                result.ammoContext(),
                result.outcome(),
                result.isHeadshot(),
                result.finalAmount()
        );
        boolean eventCanceled = MinecraftForge.EVENT_BUS.post(pre);
        if (eventCanceled) {
            event.setBaseAmount(result.finalAmount());
            ((ZDamageTypes.ZDamageSource) result.finalSource()).setSkip(true);
        } else {
            // finalAmount already includes the headshot multiplier; TaCZ must not apply it again.
            event.setBaseAmount(pre.getFinalDamage());
        }
        event.setHeadshotMultiplier(1f);
        event.setDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING, result.finalSource());
        event.setDamageSource(GunDamageSourcePart.ARMOR_PIERCING, result.finalSource());
    }
}
