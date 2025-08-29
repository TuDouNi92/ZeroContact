package net.zerocontact.events;

import com.google.common.util.concurrent.AtomicDouble;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.EntityHurtProvider;
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

    public record Caliber(String id, float baseDamageFactor, int penetrationClass, int fleshDamage) {
        public Caliber(String id, float baseDamageFactor) {
            this(id, baseDamageFactor, 10, 4);
        }
    }

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

    public static float generateDamageAmount(float original, DamageSource source, int hurtCanHold, EntityHurtProvider provider) {
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
                        output.set(balanceDamage * provider.generateBlunt());
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

    private static double getPenetratedDamage(@NotNull Caliber caliber, int hurtCanHold) {
        RandomSource randomSource = RandomSource.create();
        if (hurtCanHold >= caliber.penetrationClass) {
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
