package net.zerocontact.caliber.damage;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.caliber.damage.model.DamageResult;
import net.zerocontact.caliber.registry.MobRuleRegistry;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.compat.FirstAidCompatHandler;
import net.zerocontact.datagen.model.MobRulesPOJO;

import java.util.ArrayList;
import java.util.List;

import static net.zerocontact.caliber.damage.DamageProcessor.getHurtAmount;

public class DamagePipeLine {
    private final List<DamageModifier> plugins = new ArrayList<>();

    public DamagePipeLine() {
        plugins.addAll(List.of(
                new Modifiers.PlayerFilter(),
                new Modifiers.HeadShotProvider(),
                new Modifiers.BulletProvider(),
                new Modifiers.BulletSourceFilter(),
                new Modifiers.DamageSourceModifier(),
                new Modifiers.DamageAmountModifier(),
                new Modifiers.MobRule(),
                new Modifiers.FirstAidCptCompat()
        ));
    }

    public DamageResult process(DamageContext context) {
        DamageResultBuilder currentResult = DamageResultBuilder.create().fromContext(context);
        for (DamageModifier plugin : plugins) {
            currentResult = plugin.apply(context, currentResult);
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
                if (context.target() instanceof ServerPlayer player && player.isCreative()) {
                    builder = current.stopExecute(true);
                }
                return builder;
            }
        }

        public static class HeadShotProvider implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                EntityKineticBullet.EntityResult result = HitUtil.getHitResult(context.source());
                return current.withHeadshot(result != null && result.isHeadshot());
            }
        }

        public static class DamageSourceModifier implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                DamageSource source = ZDamageTypes.create(
                        context.target().level(),
                        context.source().getDirectEntity(),
                        context.source().getEntity(),
                        context.source().getSourcePosition());
                return current.finalSource(source);
            }
        }


        public static class BulletProvider implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                if (!context.source().is(ModDamageTypes.BULLETS_TAG)) {
                    return current.stopExecute(true);
                } else if (!context.armor().isEmpty() || !context.plate().isEmpty()) {
                    return current.withBullet(true).shouldCancelEvent(true);
                } else if (ModConfigs.SERVER.enableUniversalFleshDamage().get()) {
                    return current.withBullet(true).shouldCancelEvent(true);
                }
                return current.shouldCancelEvent(false).stopExecute(true);
            }
        }

        public static class BulletSourceFilter implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {

                //Intercepting bypass armor damage for proper damage generation and leave the general source
                if (context.source().is(ModDamageTypes.BULLET_IGNORE_ARMOR) && context.source().typeHolder().containsTag(DamageTypeTags.BYPASSES_ARMOR)) {
                    if (!context.armor().isEmpty() || !context.plate().isEmpty()) {
                        return current.shouldCancelEvent(true).stopExecute(true);
                    }

                    //Interception for unarmored entity while config enabled
                    else if (ModConfigs.SERVER.enableUniversalFleshDamage().get()) {
                        return current.shouldCancelEvent(true).stopExecute(true);
                    }

                    //final case for unarmored and disabled config
                    return current.shouldCancelEvent(false).stopExecute(true);
                }
                return current;
            }
        }

        public static class DamageAmountModifier implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                ItemStack armor = context.armor();
                ItemStack plate = context.plate();
                float finalHurtAmount = context.originalAmount();

                //Generate damage for unarmored entity
                if (ModConfigs.SERVER.enableUniversalFleshDamage().get()) {
                    finalHurtAmount = getHurtAmount(context.target(), context.source(), context.originalAmount(), null, null, 0);
                }

                DamageResultBuilder builder = current;
                if (!armor.isEmpty() || !plate.isEmpty()) {
                    //Generate damage for plate-carrier
                    if (!armor.isEmpty() && !plate.isEmpty()) {
                        if (armor.getItem() instanceof ICombatArmorItem armorProvider && plate.getItem() instanceof ICombatArmorItem plateProvider) {
                            if (armor.getMaxDamage() - armor.getDamageValue() <= 1) {
                                finalHurtAmount = getHurtAmount(context.target(), context.source(), context.originalAmount(), plateProvider, armorProvider, plateProvider.getAbsorb()) * (1 + armorProvider.generateBlunt());
                                return builder.shouldCancelEvent(true).finalAmount(finalHurtAmount);
                            }
                            builder = builder.withPlateProvider(plateProvider);
                            finalHurtAmount = getHurtAmount(context.target(), context.source(), context.originalAmount(), plateProvider, armorProvider, plateProvider.getAbsorb());
                        }
                    }
                    //Generate damage for body armor
                    else if (!armor.isEmpty()) {
                        if (armor.getItem() instanceof ICombatArmorItem armorProvider) {
                            if (armor.getMaxDamage() - armor.getDamageValue() <= 1) {
                                finalHurtAmount = getHurtAmount(context.target(), context.source(), context.originalAmount(), null, armorProvider, 0);
                                return builder.shouldCancelEvent(true).finalAmount(finalHurtAmount);
                            }
                            builder = builder.withArmorProvider(armorProvider);
                            int protectionLevel = armor.getOrCreateTag().getInt("protection_class");
                            finalHurtAmount = getHurtAmount(context.target(), context.source(), context.originalAmount(), null, armorProvider, protectionLevel);
                        }
                    }
                    //Illegal state, only plates equipped
                    else {
                        finalHurtAmount = getHurtAmount(context.target(), context.source(), context.originalAmount(), null, null, 0);
                    }
                    builder = builder.shouldCancelEvent(true);
                }

                return builder.finalAmount(finalHurtAmount);
            }
        }

        public static class MobRule implements DamageModifier {

            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                LivingEntity target = context.target();
                EntityType<?> type = target.getType();
                ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(type);
                MobRulesPOJO.Pattern mobPattern = MobRuleRegistry.get(mobId);
                if (mobPattern == null) return current;
                return current.finalAmount(
                        current.finalAmount * Math.max(0,mobPattern.bodyshotMultiplier())
                );
            }
        }

        public static class FirstAidCptCompat implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                FirstAidCompatHandler firstAidCompatHandler = FirstAidCompatHandler.create(context.target(), current.finalSource);
                if (firstAidCompatHandler == null) return current;
                if (!firstAidCompatHandler.getLimbsApplicable()) return current;
                float limbsScale = 0.25f;
                return current
                        .finalAmount(
                                getHurtAmount(
                                        context.target(),
                                        context.source(),
                                        current.finalAmount,
                                        null,
                                        null,
                                        0) * limbsScale
                        );
            }
        }

    }

    public boolean execute(DamageResult result, Runnable runnable) {
        if (result.isBullet() && !result.isHeadshot()) {
            if (result.shouldStopExecute()) return result.shouldCancelEvent();
            if (result.shouldCancelEvent()) {
                runnable.run();
            }
            return result.shouldCancelEvent();
        }
        return false;
    }
}
