package net.zerocontact.caliber.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.caliber.damage.model.DamageContext;
import net.zerocontact.caliber.damage.model.DamageResult;
import org.jetbrains.annotations.Nullable;

public class DamageResultBuilder {
    boolean isBullet;
    boolean isHeadshot;
    float finalAmount;
    DamageSource finalSource;
    boolean shouldCancelEvent;
    boolean stopExecute;
    @Nullable ICombatArmorItem armorProvider;
    @Nullable ICombatArmorItem plateProvider;

    public static DamageResultBuilder create() {
        return new DamageResultBuilder();
    }

    public DamageResultBuilder fromContext(DamageContext context) {
        this.finalAmount = context.originalAmount();
        this.finalSource = context.source();
        return this;
    }

    public DamageResultBuilder withBullet(boolean isBullet) {
        this.isBullet = isBullet;
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

    public DamageResultBuilder shouldCancelEvent(boolean shouldCancelEvent) {
        this.shouldCancelEvent = shouldCancelEvent;
        return this;
    }

    public DamageResultBuilder withArmorProvider(ICombatArmorItem armorProvider) {
        this.armorProvider = armorProvider;
        return this;
    }

    public DamageResultBuilder withPlateProvider(ICombatArmorItem plateProvider) {
        this.plateProvider = plateProvider;
        return this;
    }

    public DamageResultBuilder stopExecute(boolean stop) {
        this.stopExecute = stop;
        return this;
    }

    public DamageResult build() {
        return new DamageResult(
                isBullet,
                isHeadshot,
                finalAmount,
                finalSource,
                armorProvider,
                plateProvider,
                shouldCancelEvent,
                stopExecute
        );
    }

}
