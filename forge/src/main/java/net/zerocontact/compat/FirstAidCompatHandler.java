package net.zerocontact.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class FirstAidCompatHandler {
    private static final String MOD_ID = "firstaid";
    private static final String COMPAT_ID = "tacz_firstaid_compat";
    private final ServerPlayer player;
    private final DamageSource source;

    private FirstAidCompatHandler(ServerPlayer player, DamageSource source) {
        this.player = player;
        this.source = source;
    }

    private static boolean getCompatible() {
        return ModList.get().isLoaded(MOD_ID) && ModList.get().isLoaded(COMPAT_ID);
    }

    public boolean getLimbsApplicable() {
        return FirstAidCompatCompat.shouldBypassTorsoDamage(source, player);
    }

    public boolean getHeadApplicable() {
        return FirstAidCompatCompat.shouldInterceptHeadDamage(source, player);
    }

    public static @Nullable FirstAidCompatHandler create(LivingEntity entity, DamageSource source) {
        if (!getCompatible()) return null;
        if (!(entity instanceof ServerPlayer player)) return null;
        return new FirstAidCompatHandler(player, source);
    }
}
