package net.zerocontact.caliber;

import net.zerocontact.ZeroContactLogger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CaliberRegistry {
    private static final Map<CaliberKey, CaliberVariantDamageHelper.Caliber> CALIBERS = new HashMap<>();

    static {
        Arrays.stream(CaliberVariantDamageHelper.values()).forEach(helper -> register(helper.caliber));
    }

    public static void register(CaliberVariantDamageHelper.Caliber caliber) {
        CaliberKey key = new CaliberKey(
                caliber.id(),
                caliber.variant()
        );
        CaliberVariantDamageHelper.Caliber previous = CALIBERS.put(key, caliber);
        if (previous != null) {
            ZeroContactLogger.LOG.warn("Duplicated caliber registration!! Replacing {}", key);
        }
    }

    public static Optional<CaliberVariantDamageHelper.Caliber> get(String ammoId, String variantId) {
        return Optional.ofNullable(
                CALIBERS.get(new CaliberKey(ammoId, variantId))
        );
    }

    public static Map<CaliberKey, CaliberVariantDamageHelper.Caliber> calibers() {
        return CALIBERS;
    }


    public record CaliberKey(String id, String variant) {
    }
}
