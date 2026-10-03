package net.zerocontact.caliber.damage.model;

import net.minecraft.world.damagesource.DamageSource;
import net.zerocontact.events.HitProcessEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;

public record DamageResult(
        boolean isHeadshot,
        float finalAmount,
        DamageSource finalSource,
        HitProcessEvent.EventArmorContext armorContext,
        HitProcessEvent.EventAmmoContext ammoContext,
        HitProcessEvent.ZHitOutcome outcome,
        boolean shouldReplaceDamage,
        boolean shouldStopExecute,
        HitPart hitPart
) {
}
