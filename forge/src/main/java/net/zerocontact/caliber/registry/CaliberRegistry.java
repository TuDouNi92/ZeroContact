package net.zerocontact.caliber.registry;

import net.zerocontact.ZeroContactLogger;
import net.zerocontact.caliber.CaliberHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CaliberRegistry {
    private static final Map<CaliberKey, CaliberHelper.Caliber> CALIBERS = new HashMap<>();

    static {
        Arrays.stream(CaliberHelper.values()).forEach(helper -> register(helper.caliber));
    }

    public static void register(CaliberHelper.Caliber caliber) {
        CaliberKey key = new CaliberKey(
                caliber.id(),
                caliber.variant()
        );
        CaliberHelper.Caliber previous = CALIBERS.put(key, caliber);
        if (previous != null) {
            ZeroContactLogger.LOG.warn("Duplicated caliber registration!! Replacing {}", key);
        }
    }

    public static Optional<CaliberHelper.Caliber> get(String ammoId, String variantId) {
        return Optional.ofNullable(
                CALIBERS.get(new CaliberKey(ammoId, variantId))
        );
    }

    public static Map<CaliberKey, CaliberHelper.Caliber> calibers() {
        return CALIBERS;
    }


    public record CaliberKey(String id, String variant) {
    }
}
