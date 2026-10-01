package net.zerocontact.caliber.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.items.IItemHandler;

import java.util.function.BooleanSupplier;

public final class MagazineReloadCheck {
    private static final ThreadLocal<LivingEntity> SHOOTER = new ThreadLocal<>();

    private MagazineReloadCheck() {
    }

    public static boolean withShooter(LivingEntity shooter, BooleanSupplier check) {
        LivingEntity previous = SHOOTER.get();
        SHOOTER.set(shooter);
        try {
            return check.getAsBoolean();
        } finally {
            if (previous == null) {
                SHOOTER.remove();
            } else {
                SHOOTER.set(previous);
            }
        }
    }

    public static IItemHandler resolveInventory(IItemHandler fallback) {
        LivingEntity shooter = SHOOTER.get();
        return shooter == null ? fallback : ReloadManager.resolveReloadInv(shooter).rawHandler();
    }
}
