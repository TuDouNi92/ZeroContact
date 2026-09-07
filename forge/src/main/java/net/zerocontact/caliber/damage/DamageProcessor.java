package net.zerocontact.caliber.damage;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.caliber.CaliberHelper;
import net.zerocontact.caliber.registry.CaliberRegistry;
import net.zerocontact.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
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
    private static float generateDamageAmount(float original, DamageSource source, int hurtCanHold, @Nullable ICombatArmorItem provider) {
        AtomicDouble output = new AtomicDouble(original);
        Optional.ofNullable(source.getDirectEntity()).ifPresent(bullet -> {
            if (bullet.level() instanceof ServerLevel serverLevel) {
                if (CommandManager.CommandSavedData.get(serverLevel).experimentalBallistic) {
                    Set<CaliberHelper.Caliber> mergedCaliberSet = CaliberHelper.CALIBER_HELPER_ENUM_SET.stream().map(a -> a.caliber).collect(Collectors.toSet());
                    mergedCaliberSet.removeAll(CaliberRegistry.calibers().values());
                    mergedCaliberSet.addAll(CaliberRegistry.calibers().values());
                    CaliberHelper.getMatchedCaliber(source, mergedCaliberSet).ifPresent(caliber -> {
                        double penetratedDamage = getPenetratedDamage(caliber, hurtCanHold);
                        setOutput(provider, caliber, penetratedDamage, output);
                    });
                } else {
                    CaliberHelper.getMatchedCaliber(source, CaliberHelper.CALIBER_HELPER_ENUM_SET).ifPresent(caliber -> {
                        double penetratedDamage = getPenetratedDamage(caliber, hurtCanHold);
                        setOutput(provider, caliber, penetratedDamage, output);
                    });
                }
            }

        });
        return (float) output.get();
    }

    private static void setOutput(@Nullable ICombatArmorItem provider, CaliberHelper.Caliber caliber, double penetratedDamage, AtomicDouble output) {
        if (penetratedDamage > 0) {
            if (provider == null) {
                output.set(penetratedDamage);
            } else {
                output.set(penetratedDamage * provider.generatePenetrated());
            }
        } else {
            if (provider == null) {
                output.set(caliber.fleshDamage());
            } else {
                output.set(caliber.penetrationClass() * 0.3 * provider.generateBlunt());
            }
        }
    }

    /**
     * This method generates the damage once armor get penetrated
     *
     * @param caliber     Caliber class
     * @param hurtCanHold The damage that armor/plate can withstand
     * @return Determine and returns the flesh damage
     */
    private static double getPenetratedDamage(@NotNull CaliberHelper.Caliber caliber, int hurtCanHold) {
        RandomSource randomSource = RandomSource.create();
        if (hurtCanHold >= caliber.penetrationClass()) {
            double preOdds = 0.42139
                    + 2.00643 * caliber.penetrationClass()
                    - 1.80617 * hurtCanHold;
            double penetrateOdds = 1.0 / (1.0 + Math.exp(-preOdds));
            if (randomSource.nextFloat() < penetrateOdds) {
                return caliber.fleshDamage();
            }
            return 0.0;
        } else {
            return caliber.fleshDamage();
        }
    }

    public static float getHurtAmount(LivingEntity lv, DamageSource source, float amount, @Nullable ICombatArmorItem plateProvider, @Nullable ICombatArmorItem armorProvider, int hurtCanHold) {
        float hurtAmount;
        float generateCaliberDamageAmount;
        if (plateProvider != null && armorProvider != null) {
            generateCaliberDamageAmount = generateDamageAmount(amount, source, hurtCanHold, plateProvider);
        } else if (armorProvider != null) {
            generateCaliberDamageAmount = generateDamageAmount(amount, source, hurtCanHold, armorProvider);
        } else {
            generateCaliberDamageAmount = generateDamageAmount(amount, source, hurtCanHold, null);
        }
        if (armorProvider != null && HitUtil.isIncidentAngleValid(lv, source)) {
            hurtAmount = armorProvider.generateRicochet() * generateCaliberDamageAmount;
        } else {
            hurtAmount = generateCaliberDamageAmount;
        }
        return hurtAmount;
    }
}
