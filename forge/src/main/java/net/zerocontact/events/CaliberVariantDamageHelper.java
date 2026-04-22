package net.zerocontact.events;

import com.google.common.util.concurrent.AtomicDouble;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.command.CommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public enum CaliberVariantDamageHelper {
    CALIBER_762x39(
            new Caliber("tacz:762x39", 3)
    ),
    CALIBER_556x45(
            new Caliber("tacz:556x45", 2)
    ),
    CALIBER_308(
            new Caliber("tacz:308", 4)
    ),
    CALIBER_50AE(
            new Caliber("tacz:50ae", 2)
    ),
    CALIBER_9mm(
            new Caliber("tacz:9mm", 1.5f)
    ),
    CALIBER_45ACP(
            new Caliber("tacz:45acp", 1.25f)
    ),
    CALIBER_762x25(
            new Caliber("tacz:762x25", 1.5f)
    ),
    CALIBER_762x54(
            new Caliber("tacz:762x54", 4)
    ),
    CALIBER_338(
            new Caliber("tacz:338", 3)
    ),
    CALIBER_68x51(
            new Caliber("tacz:6.8x51", 2.5f)
    ),
    CALIBER_50BMG(
            new Caliber("tacz:50bmg", 4)
    );
    private final Caliber caliber;
    private static final EnumSet<CaliberVariantDamageHelper> caliberVariantDamageHelperEnumSet = EnumSet.of(
            CALIBER_556x45, CALIBER_762x39, CALIBER_68x51, CALIBER_308, CALIBER_338, CALIBER_762x54, CALIBER_45ACP, CALIBER_762x25, CALIBER_9mm, CALIBER_50AE, CALIBER_50BMG
    );
    public static final Set<Caliber> experimentalBallisticSet = new HashSet<>();

    CaliberVariantDamageHelper(Caliber caliber) {
        this.caliber = caliber;
    }

    /**
     * Represents the caliber infos of a type of ammo
     *
     * <p>This class is meant to create a middleware that modifies ammo damage associated with gun data</p>
     *
     * @param id The ammo id from TAC:Z guns
     * @param baseDamageFactor The balancing factor for each caliber generating new numbers
     * @param penetrationClass The penetration level for damage interceptor, bypassed when the feature is off
     * @param fleshDamage The flesh damage for damage interceptor, bypassed when the feature is off
     */
    public record Caliber(String id, float baseDamageFactor, int penetrationClass, int fleshDamage) {
        public Caliber(String id, float baseDamageFactor) {
            this(id, baseDamageFactor, 10, 4);
        }
    }

    /**
     *
     * <p>This method is meant to match calibers with input bullet damage source </p>
     *
     * @param source The Minecraft damage source
     * @param set The enum set for calibers defined
     * @return Return the caliber that matched with damage source
     * @param <E> The enum set type
     */
    private static <E> Optional<Caliber> getMatchedCaliber(DamageSource source, Set<E> set) {
        AtomicReference<Optional<Caliber>> result = new AtomicReference<>(Optional.empty());
        if (!source.is(ModDamageTypes.BULLETS_TAG)) return Optional.empty();
        Optional.ofNullable(source.getDirectEntity()).ifPresent(entity -> {
            if (entity instanceof EntityKineticBullet bullet) {
                for (E caliberData : set) {
                    if (caliberData instanceof CaliberVariantDamageHelper caliberEnum) {
                        if (caliberEnum.caliber.id.equals(bullet.getAmmoId().toString())) {
                            result.set(Optional.of(caliberEnum.caliber));
                        }

                    } else if (caliberData instanceof Caliber caliber) {
                        if (caliber.id.equals(bullet.getAmmoId().toString())) {
                            result.set(Optional.of(caliber));
                        }
                    }
                }
            }

        });
        return result.get();
    }

    /**
     *
     * <p>This method is meant to generate damages under the effect of protections</p>
     *
     * @param original The original bullet damage
     * @param source The Minecraft damage source
     * @param hurtCanHold The damage that armor/plate can withstand
     * @param provider Interface implementation that provides the situation of getting hit by bullets
     * @return The generated damage amount
     */
    public static float generateDamageAmount(float original, DamageSource source, int hurtCanHold, ICombatArmorItem provider) {
        AtomicDouble output = new AtomicDouble(original);
        Optional.ofNullable(source.getDirectEntity()).ifPresent(bullet -> {
            if (bullet.level() instanceof ServerLevel serverLevel) {
                if (CommandManager.CommandSavedData.get(serverLevel).experimentalBallistic) {
                    getMatchedCaliber(source, experimentalBallisticSet).ifPresent(caliber -> {
                        double balanceDamage = caliber.baseDamageFactor * Mth.sqrt(original) * 0.75f;
                        double fleshDamage = getPenetratedDamage(caliber, hurtCanHold);
                        if (fleshDamage != 0) {
                            output.set(fleshDamage * provider.generatePenetrated());
                        }
                        else{
                            output.set(balanceDamage * provider.generateBlunt());
                        }
                    });
                } else {
                    getMatchedCaliber(source, caliberVariantDamageHelperEnumSet).ifPresent(caliber -> {
                        double balanceDamage = caliber.baseDamageFactor * Mth.sqrt(original) * 0.75f;
                        if (hurtCanHold > balanceDamage) {
                            output.set(balanceDamage * provider.generateBlunt());
                        } else {
                            output.set(balanceDamage * provider.generatePenetrated());
                        }
                    });
                }
            }

        });
        return (float) output.get();
    }

    /**
     *
     * This method generates the damage once armor get penetrated
     *
     * @param caliber Caliber class
     * @param hurtCanHold The damage that armor/plate can withstand
     * @return Determine and returns the flesh damage
     */
    private static double getPenetratedDamage(@NotNull Caliber caliber, int hurtCanHold) {
        RandomSource randomSource = RandomSource.create();
        if (hurtCanHold > caliber.penetrationClass) {
            double penetrateProbability = Math.pow((double) caliber.penetrationClass / hurtCanHold, 2);
            if (randomSource.nextFloat() <= penetrateProbability) {
                return caliber.fleshDamage;
            }
            return 0.0;
        } else {
            return caliber.fleshDamage;
        }
    }
}
