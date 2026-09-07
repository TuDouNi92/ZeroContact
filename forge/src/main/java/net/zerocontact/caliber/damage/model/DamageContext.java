package net.zerocontact.caliber.damage.model;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DamageContext(
        LivingEntity target,
        DamageSource source,
        float originalAmount,
        @NotNull ItemStack plate,
        @NotNull ItemStack armor
) {
}
