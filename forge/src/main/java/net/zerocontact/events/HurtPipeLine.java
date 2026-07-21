package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.compat.FirstAidCompatHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.zerocontact.events.PlateEntityHurtEvent.getHurtAmount;

public class HurtPipeLine {
    private final List<DamageModifier> plugins = new ArrayList<>();

    public HurtPipeLine() {
        plugins.addAll(List.of(
                new Modifiers.PlayerFilter(),
                new Modifiers.HeadShotProvider(),
                new Modifiers.BulletProvider(),
                new Modifiers.BulletSourceFilter(),
                new Modifiers.DamageSourceModifier(),
                new Modifiers.DamageAmountModifier(),
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
                if (context.target instanceof ServerPlayer player && player.isCreative()) {
                    builder = current.setStopPipe(true);
                }
                return builder;
            }
        }

        public static class HeadShotProvider implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                EntityKineticBullet.EntityResult result = EventUtil.getHitResult(context.source);
                return current.withHeadshot(result != null && result.isHeadshot());
            }
        }

        public static class DamageSourceModifier implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                DamageSource source = ZDamageTypes.create(
                        context.target.level(),
                        context.source.getDirectEntity(),
                        context.source.getEntity(),
                        context.source.getSourcePosition());
                return current.finalSource(source);
            }
        }


        public static class BulletProvider implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                if (!context.source.is(ModDamageTypes.BULLETS_TAG)) return current.setStopPipe(true);
                return current.withBullet(true).shouldCancelEvent(true);
            }
        }

        public static class BulletSourceFilter implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                if (
                        context.source.is(ModDamageTypes.BULLET_IGNORE_ARMOR) && context.source.typeHolder().containsTag(DamageTypeTags.BYPASSES_ARMOR)
                ) {
                    return current.shouldCancelEvent(true).setStopPipe(true);
                }
                return current;
            }
        }

        public static class DamageAmountModifier implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                ItemStack armor = context.armor;
                ItemStack plate = context.plate;
                float finalHurtAmount = context.originalAmount;
                DamageResultBuilder builder = current;
                if (armor != null || plate != null) {
                    if (armor != null && plate != null) {
                        if (armor.getItem() instanceof ICombatArmorItem armorProvider && plate.getItem() instanceof ICombatArmorItem plateProvider) {
                            if (armor.getMaxDamage() - armor.getDamageValue() <= 1) {
                                finalHurtAmount = getHurtAmount(context.target, context.source, context.originalAmount, plateProvider, armorProvider, plateProvider.getAbsorb()) * (1 + armorProvider.generateBlunt());
                                return builder.shouldCancelEvent(true).finalAmount(finalHurtAmount);
                            }
                            builder = builder.withPlateProvider(plateProvider);
                            finalHurtAmount = getHurtAmount(context.target, context.source, context.originalAmount, plateProvider, armorProvider, plateProvider.getAbsorb());
                        }
                    } else if (armor != null) {
                        if (armor.getItem() instanceof ICombatArmorItem armorProvider) {
                            if (armor.getMaxDamage() - armor.getDamageValue() <= 1) {
                                finalHurtAmount = getHurtAmount(context.target, context.source, context.originalAmount, null, armorProvider, 0);
                                return builder.shouldCancelEvent(true).finalAmount(finalHurtAmount);
                            }
                            builder = builder.withArmorProvider(armorProvider);
                            int protectionLevel = armor.getOrCreateTag().getInt("protection_class");
                            finalHurtAmount = getHurtAmount(context.target, context.source, context.originalAmount, null, armorProvider, protectionLevel);
                        }
                    } else {
                        finalHurtAmount = getHurtAmount(context.target, context.source, context.originalAmount, null, null, 0);
                    }
                    builder = builder.shouldCancelEvent(true);
                }

                return builder.finalAmount(finalHurtAmount);
            }
        }

        public static class FirstAidCptCompat implements DamageModifier {
            @Override
            public DamageResultBuilder apply(DamageContext context, DamageResultBuilder current) {
                FirstAidCompatHandler firstAidCompatHandler = FirstAidCompatHandler.create(context.target, current.finalSource);
                if (firstAidCompatHandler == null) return current;
                if (!firstAidCompatHandler.getLimbsApplicable()) return current;
                float limbsScale = 0.25f;
                return current
                        .finalAmount(
                                getHurtAmount(
                                        context.target,
                                        context.source,
                                        current.finalAmount,
                                        null,
                                        null,
                                        0) * limbsScale
                        );
            }
        }

    }

    public record DamageContext(
            LivingEntity target,
            DamageSource source,
            float originalAmount,
            @Nullable ItemStack plate,
            @Nullable ItemStack armor
    ) {
    }

    public record DamageResult(
            boolean isBullet,
            boolean isHeadshot,
            float finalAmount,
            DamageSource finalSource,
            @Nullable ICombatArmorItem armorProvider,
            @Nullable ICombatArmorItem plateProvider,
            boolean shouldCancelEvent,
            boolean shouldStopPipe
    ) {
    }

    public static class DamageResultBuilder {
        boolean isBullet;
        boolean isHeadshot;
        float finalAmount;
        DamageSource finalSource;
        boolean shouldCancelEvent;
        boolean shouldStopPipeline;
        @Nullable ICombatArmorItem armorProvider;
        @Nullable ICombatArmorItem plateProvider;

        public static DamageResultBuilder create() {
            return new DamageResultBuilder();
        }

        public DamageResultBuilder fromContext(DamageContext context) {
            this.finalAmount = context.originalAmount;
            this.finalSource = context.source;
            return this;
        }

        public DamageResultBuilder withBullet(boolean isBullet) {
            this.isBullet = isBullet;
            return this;
        }

        public DamageResultBuilder withHeadshot(boolean isHeadshot) {
            this.isHeadshot = isHeadshot;
            return this;
        }

        public DamageResultBuilder finalAmount(float finalAmount) {
            this.finalAmount = finalAmount;
            return this;
        }

        public DamageResultBuilder finalSource(DamageSource source) {
            this.finalSource = source;
            return this;
        }

        public DamageResultBuilder shouldCancelEvent(boolean shouldCancelEvent) {
            this.shouldCancelEvent = shouldCancelEvent;
            return this;
        }

        public DamageResultBuilder withArmorProvider(ICombatArmorItem armorProvider) {
            this.armorProvider = armorProvider;
            return this;
        }

        public DamageResultBuilder withPlateProvider(ICombatArmorItem plateProvider) {
            this.plateProvider = plateProvider;
            return this;
        }

        public DamageResultBuilder setStopPipe(boolean stopPipe) {
            this.shouldStopPipeline = stopPipe;
            return this;
        }

        public DamageResult build() {
            return new DamageResult(
                    isBullet,
                    isHeadshot,
                    finalAmount,
                    finalSource,
                    armorProvider,
                    plateProvider,
                    shouldCancelEvent,
                    shouldStopPipeline
            );
        }

    }

    public boolean execute(DamageResult result, Runnable runnable) {
        if (result.isBullet() && !result.isHeadshot()) {
            if (result.shouldStopPipe()) return result.shouldCancelEvent();
            if (result.shouldCancelEvent()) {
                runnable.run();
            }
            return result.shouldCancelEvent();
        }
        return false;
    }
}
