package net.zerocontact.caliber.damage;

import com.tacz.guns.api.event.common.GunDamageSourcePart;
import net.minecraft.world.damagesource.DamageSource;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.caliber.damage.model.DamageResult;
import net.zerocontact.events.HitProcessEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;

public class DamageResultBuilder {
    boolean isHeadshot;
    float finalAmount;
    DamageSource finalSource;
    HitProcessEvent.EventArmorContext armorContext;
    HitProcessEvent.EventAmmoContext ammoContext;
    HitProcessEvent.ZHitOutcome outcome;
    boolean shouldReplaceDamage;
    boolean stopExecute;
    HitPart hitPart;

    public static DamageResultBuilder create() {
        return new DamageResultBuilder();
    }

    public DamageResultBuilder fromContext(DamageContext context) {
        fromCalculation(DamageProcessor.DamageCalcCtx.unprocessed(context.event().getBaseAmount()));
        this.isHeadshot = context.event().isHeadShot();
        this.hitPart = context.hitPart();
        this.finalSource = context.event().getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
        this.armorContext = new HitProcessEvent.EventArmorContext(context.armor(), context.plate(), 0, 0);
        return this;
    }

    /** Copies the metadata from the same calculation that produced the damage. */
    public DamageResultBuilder fromCalculation(DamageProcessor.DamageCalcCtx calculation) {
        this.finalAmount = calculation.outputDamage();
        this.outcome = calculation.outcome();
        this.ammoContext = new HitProcessEvent.EventAmmoContext(
                calculation.caliberId(), calculation.caliberVariant(), calculation.penetrationLevel(),
                calculation.fleshDamage(), calculation.armorDamage());
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

    public DamageResultBuilder shouldReplaceDamage(boolean shouldReplaceDamage) {
        this.shouldReplaceDamage = shouldReplaceDamage;
        return this;
    }



    public DamageResultBuilder stopExecute(boolean stop) {
        this.stopExecute = stop;
        return this;
    }


    public DamageResultBuilder setArmorContext(HitProcessEvent.EventArmorContext armorContext) {
        this.armorContext = armorContext;
        return this;
    }

    public DamageResult build() {
        return new DamageResult(
                isHeadshot,
                finalAmount,
                finalSource,
                armorContext,
                ammoContext,
                outcome,
                shouldReplaceDamage,
                stopExecute,
                hitPart
        );
    }

}
