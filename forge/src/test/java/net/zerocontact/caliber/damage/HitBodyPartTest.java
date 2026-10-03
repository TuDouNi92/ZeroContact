package net.zerocontact.caliber.damage;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.compat.FirstAidCompatCompat;
import net.zerocontact.compat.FirstAidCompatHandler;
import net.zerocontact.events.HitProcessEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPartEnum;

/** Standalone body-part/damage regressions; no game world or loaded mod list required. */
public final class HitBodyPartTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        // The listener's public API must load even when optional mods are absent.
        check(FirstAidCompatHandler.class.getDeclaredMethods().length > 0, "listener API loads");
        if (args.length == 0) {
            check(FirstAidCompatCompat.toHitPart(EnumPlayerPart.HEAD) == HitPartEnum.HEAD, "head mapping");
            check(FirstAidCompatCompat.toHitPart(EnumPlayerPart.BODY) == HitPartEnum.TORSO, "torso mapping");
            for (var part : new EnumPlayerPart[]{EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM}) {
                check(FirstAidCompatCompat.toHitPart(part) == HitPartEnum.ARM, "both arms bypass chest armor");
            }
            for (var part : new EnumPlayerPart[]{EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG,
                    EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT}) {
                check(FirstAidCompatCompat.toHitPart(part) == HitPartEnum.LEG, "legs and feet bypass chest armor");
            }
        }

        verifyDamage(HitPartEnum.ARM, 9f, .25f, 2.25f, true);
        verifyDamage(HitPartEnum.LEG, 9f, .25f, 2.25f, true);
        verifyDamage(HitPartEnum.HEAD, 6f, .2f, 1.2f, true);
        verifyDamage(HitPartEnum.TORSO, 4f, 1f, 4f, false);
        verifyDamage(HitPartEnum.HEAD, 4f, 0f, 0f, true);

        for (float invalid : new float[]{-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            try {
                new HitPart(HitPartEnum.TORSO, invalid);
                throw new AssertionError("invalid factor accepted: " + invalid);
            } catch (IllegalArgumentException expected) {
                checks++;
            }
        }
        System.out.println("HitBodyPart: " + checks + " regression checks passed");
    }

    private static void verifyDamage(HitPartEnum part, float amount, float factor, float expected, boolean replace) {
        HitPart hitPart = new HitPart(part, factor);
        // This stage must use the calculated damage, not recalculate it from the caliber.
        var context = new DamageContext(null, null, null, hitPart);
        var current = DamageResultBuilder.create().finalAmount(amount).withHeadshot(part == HitPartEnum.HEAD);
        current.hitPart = hitPart;
        current.outcome = HitProcessEvent.ZHitOutcome.NON_PEN;
        var result = new DamagePipeLine.Modifiers.HitPartDamageFactor().apply(context, current).build();
        check(Math.abs(result.finalAmount() - expected) < .00001f, "factor applies once to calculated damage");
        check(result.shouldReplaceDamage() == replace, "changed damage reaches the TaCZ event");
        check(result.outcome() == HitProcessEvent.ZHitOutcome.NON_PEN, "scaling preserves penetration outcome");
        check(result.hitPart() == hitPart, "resolved part remains available for armor durability and sound");
        check(result.isHeadshot() == (part == HitPartEnum.HEAD), "scaling preserves resolved headshot flag");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
