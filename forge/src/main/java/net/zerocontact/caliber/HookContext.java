package net.zerocontact.caliber;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record HookContext(
        ServerLevel level,
        @Nullable LivingEntity shooter,
        @Nullable LivingEntity victim,
        Vec3 positon,
        CaliberVariantDamageHelper.Caliber caliber
        ) {
}
