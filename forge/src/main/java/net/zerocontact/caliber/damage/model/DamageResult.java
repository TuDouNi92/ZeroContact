package net.zerocontact.caliber.damage.model;

import net.minecraft.world.damagesource.DamageSource;
import net.zerocontact.api.ICombatArmorItem;
import org.jetbrains.annotations.Nullable;

public record DamageResult(
        boolean isBullet,
        boolean isHeadshot,
        float finalAmount,
        DamageSource finalSource,
        @Nullable ICombatArmorItem armorProvider,
        @Nullable ICombatArmorItem plateProvider,
        boolean shouldCancelEvent,
        boolean shouldStopExecute
) {
}
