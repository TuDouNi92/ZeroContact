package net.zerocontact.caliber.damage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.caliber.CaliberHelper;
import net.zerocontact.caliber.registry.CaliberRegistry;
import net.zerocontact.command.CommandManager;
import net.zerocontact.events.HitProcessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DamageProcessor {
    /**
     * <p>This method is meant to generate damages under the effect of protections</p>
     *
     * @param original    The original bullet damage
     * @param source      The Minecraft damage source
     * @param hurtCanHold The damage that armor/plate can withstand
     * @param provider    Interface implementation that provides the situation of getting hit by bullets
     * @return The generated damage amount
     */
    private static DamageCalcCtx generateDamageAmount(float original, DamageSource source, int hurtCanHold, @Nullable ICombatArmorItem provider) {
        AtomicReference<DamageCalcCtx> output = new AtomicReference<>(DamageCalcCtx.unprocessed(original));
        Optional.ofNullable(source.getDirectEntity()).ifPresent(bullet -> {
            if (bullet.level() instanceof ServerLevel serverLevel) {
                if (CommandManager.CommandSavedData.get(serverLevel).experimentalBallistic) {
                    Set<CaliberHelper.Caliber> mergedCaliberSet = CaliberHelper.CALIBER_HELPER_ENUM_SET.stream().map(a -> a.caliber).collect(Collectors.toSet());
                    mergedCaliberSet.removeAll(CaliberRegistry.calibers().values());
                    mergedCaliberSet.addAll(CaliberRegistry.calibers().values());
                    CaliberHelper.getMatchedCaliber(source, mergedCaliberSet).ifPresent(caliber -> {
                        DamageCalcCtx calcCtx = getPenetratedCtx(caliber, hurtCanHold);
                        double penetratedDamage = calcCtx.outputDamage();
                        setOutput(provider, caliber, penetratedDamage, calcCtx, output);
                    });
                } else {
                    CaliberHelper.getMatchedCaliber(source, CaliberHelper.CALIBER_HELPER_ENUM_SET).ifPresent(caliber -> {
                        DamageCalcCtx calcCtx = getPenetratedCtx(caliber, hurtCanHold);
                        double penetratedDamage = calcCtx.outputDamage();
                        setOutput(provider, caliber, penetratedDamage, calcCtx, output);
                    });
                }
            }

        });
        return output.get();
    }

    private static void setOutput(@Nullable ICombatArmorItem provider, CaliberHelper.Caliber caliber, double penetratedDamage, DamageCalcCtx raw, AtomicReference<DamageCalcCtx> output) {
        double outputDamage;
        if (penetratedDamage > 0) {
            if (provider == null) {
                outputDamage = penetratedDamage;
            } else {
                outputDamage = (penetratedDamage * provider.generatePenetrated());
            }
        } else {
            if (provider == null) {
                outputDamage = caliber.fleshDamage();
            } else {
                outputDamage = caliber.penetrationClass() * 0.3 * provider.generateBlunt();
            }
        }
        output.set(
                new DamageCalcCtx(
                        raw.outcome,
                        raw.caliberId,
                        raw.caliberVariant,
                        raw.penetrationLevel,
                        raw.fleshDamage,
                        raw.armorDamage,
                        (float) outputDamage
                )
        );
    }


    public record DamageCalcCtx(
            HitProcessEvent.ZHitOutcome outcome,
            ResourceLocation caliberId,
            ResourceLocation caliberVariant,
            int penetrationLevel,
            float fleshDamage,
            float armorDamage,
            float outputDamage
    ) {
        public static DamageCalcCtx unprocessed(float damage) {
            ResourceLocation unknown = new ResourceLocation("zerocontact", "unknown");
            return new DamageCalcCtx(HitProcessEvent.ZHitOutcome.NO_OUTCOME,
                    unknown, unknown, 0, 0, 0, damage);
        }

        public DamageCalcCtx withOutputDamage(float damage) {
            return new DamageCalcCtx(outcome, caliberId, caliberVariant,
                    penetrationLevel, fleshDamage, armorDamage, damage);
        }
    }

    /**
     * This method generates the damage once armor get penetrated
     *
     * @param caliber     Caliber class
     * @param hurtCanHold The damage that armor/plate can withstand
     * @return Determine and returns the flesh damage
     */
    private static DamageCalcCtx getPenetratedCtx(@NotNull CaliberHelper.Caliber caliber, int hurtCanHold) {
        RandomSource randomSource = RandomSource.create();
        if (hurtCanHold >= caliber.penetrationClass()) {
            double preOdds = 0.42139
                    + 2.00643 * caliber.penetrationClass()
                    - 1.80617 * hurtCanHold;
            double penetrateOdds = 1.0 / (1.0 + Math.exp(-preOdds));
            if (randomSource.nextFloat() < penetrateOdds) {
                return new DamageCalcCtx(
                        hurtCanHold == 0 ? HitProcessEvent.ZHitOutcome.NO_ARMOR : HitProcessEvent.ZHitOutcome.PEN,
                        new ResourceLocation(caliber.id()),
                        new ResourceLocation(caliber.variant()),
                        caliber.penetrationClass(),
                        caliber.fleshDamage(),
                        caliber.armorDamage(),
                        caliber.fleshDamage()
                );
            }
            return new DamageCalcCtx(
                    HitProcessEvent.ZHitOutcome.NON_PEN,
                    new ResourceLocation(caliber.id()),
                    new ResourceLocation(caliber.variant()),
                    caliber.penetrationClass(),
                    caliber.fleshDamage(),
                    caliber.armorDamage(),
                    0.0f
            );
        } else {
            return new DamageCalcCtx(
                    hurtCanHold <= 0 ? HitProcessEvent.ZHitOutcome.NO_ARMOR : HitProcessEvent.ZHitOutcome.PEN,
                    new ResourceLocation(caliber.id()),
                    new ResourceLocation(caliber.variant()),
                    caliber.penetrationClass(),
                    caliber.fleshDamage(),
                    caliber.armorDamage(),
                    caliber.fleshDamage()
            );
        }
    }

    public static DamageCalcCtx getHurtAmount(Entity lv, DamageSource source, float amount, @Nullable ICombatArmorItem plateProvider, @Nullable ICombatArmorItem armorProvider, int hurtCanHold) {
        DamageCalcCtx finalCtx;
        DamageCalcCtx generateCaliberDamage;
        if (plateProvider != null && armorProvider != null) {
            generateCaliberDamage = generateDamageAmount(amount, source, hurtCanHold, plateProvider);
        } else if (armorProvider != null) {
            generateCaliberDamage = generateDamageAmount(amount, source, hurtCanHold, armorProvider);
        } else {
            generateCaliberDamage = generateDamageAmount(amount, source, hurtCanHold, null);
        }
        if (armorProvider != null && HitUtil.isIncidentAngleValid(lv, source)) {
            finalCtx = new DamageCalcCtx(
                    HitProcessEvent.ZHitOutcome.RICOCHET,
                    generateCaliberDamage.caliberId(),
                    generateCaliberDamage.caliberVariant(),
                    generateCaliberDamage.penetrationLevel(),
                    generateCaliberDamage.fleshDamage(),
                    generateCaliberDamage.armorDamage(),
                    generateCaliberDamage.outputDamage()
            );
        } else {
            finalCtx = generateCaliberDamage;
        }
        return finalCtx;
    }
}
