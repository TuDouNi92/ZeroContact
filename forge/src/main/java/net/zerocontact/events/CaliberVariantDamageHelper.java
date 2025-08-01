package net.zerocontact.events;

import com.google.common.util.concurrent.AtomicDouble;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public enum CaliberVariantDamageHelper {
    CALIBER_762x39(
            new Caliber("tacz:762x39", 4)
    ),
    CALIBER_556x45(
            new Caliber("tacz:556x45", 3)
    ),
    CALIBER_308(
            new Caliber("tacz:308", 5)
    ),
    CALIBER_50AE(
            new Caliber("tacz:50ae", 3)
    ),
    CALIBER_9mm(
            new Caliber("tacz:9mm", 2.5f)
    ),
    CALIBER_45ACP(
            new Caliber("tacz:45acp", 2.25f)
    ),
    CALIBER_762x25(
            new Caliber("tacz:762x25", 2.5f)
    ),
    CALIBER_762x54(
            new Caliber("tacz:762x54", 5)
    ),
    CALIBER_338(
            new Caliber("tacz:338",4)
    ),
    CALIBER_68x51(
            new Caliber("tacz:6.8x51", 3.5f)
    ),
    CALIBER_50BMG(
            new Caliber("tacz:50bmg", 5)
    );
    private final Caliber caliber;
    private static final EnumSet<CaliberVariantDamageHelper> caliberVariantDamageHelperEnumSet = EnumSet.of(
            CALIBER_556x45, CALIBER_762x39, CALIBER_68x51, CALIBER_308,CALIBER_338, CALIBER_762x54, CALIBER_45ACP, CALIBER_762x25, CALIBER_9mm, CALIBER_50AE, CALIBER_50BMG
    );

    CaliberVariantDamageHelper(Caliber caliber) {
        this.caliber = caliber;
    }

    private record Caliber(String id, float baseDamageFactor) {
    }

    private static Optional<CaliberVariantDamageHelper> getMatchedCaliber(DamageSource source) {
        AtomicReference<Optional<CaliberVariantDamageHelper>> result = new AtomicReference<>(Optional.empty());
        if (!source.is(ModDamageTypes.BULLET)) return Optional.empty();
        Optional.ofNullable(source.getDirectEntity()).ifPresent(entity -> {
            if (entity instanceof EntityKineticBullet bullet) {
                for (CaliberVariantDamageHelper caliberEnum : caliberVariantDamageHelperEnumSet) {
                    if (caliberEnum.caliber.id.equals(bullet.getAmmoId().toString())) {
                        result.set(Optional.of(caliberEnum));
                    }
                }
            }

        });
        return result.get();
    }

    public static float generateDamageAmount(float original, DamageSource source) {
        AtomicDouble output = new AtomicDouble(original);
        getMatchedCaliber(source).ifPresent(caliberVariantDamageHelper ->
                output.set(caliberVariantDamageHelper.caliber.baseDamageFactor * Mth.sqrt(original) * 0.75f));
        return (float) output.get();
    }
}
