package net.zerocontact.caliber.extension.model;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.caliber.CaliberHelper;
import org.jetbrains.annotations.Nullable;

public record HookContext(
        ServerLevel level,
        @Nullable LivingEntity shooter,
        @Nullable LivingEntity victim,
        @Nullable Vec3 prevPos,
        Vec3 positon,
        CaliberHelper.Caliber caliber
) {
    public HookContext(ServerLevel level,
                @Nullable LivingEntity shooter,
                @Nullable LivingEntity victim,
                Vec3 positon,
                CaliberHelper.Caliber caliber) {
        this(
                level,
                shooter,
                victim,
                null,
                positon,
                caliber
        );
    }
}
