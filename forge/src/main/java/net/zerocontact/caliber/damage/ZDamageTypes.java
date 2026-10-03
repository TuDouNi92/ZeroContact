package net.zerocontact.caliber.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.caliber.damage.model.DamageResult;
import org.jetbrains.annotations.Nullable;


import static net.zerocontact.ZeroContact.MOD_ID;

public class ZDamageTypes {
    public static final ResourceKey<DamageType> ZC_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "zc_damage"));

    public static DamageSource create(
            DamageResult result,
            Level level,
            Entity directEntity,
            Entity causingEntity,
            Vec3 sourcePosition,
            boolean skip
    ) {
        return new ZDamageSource(result,
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ZDamageTypes.ZC_DAMAGE),
                directEntity,
                causingEntity,
                sourcePosition,
                skip
        );
    }

    public static class ZDamageSource extends DamageSource {
        private final DamageResult lastResult;
        private boolean skip;

        public ZDamageSource(
                DamageResult lastResult,
                Holder<DamageType> type,
                @Nullable Entity directEntity,
                @Nullable Entity causingEntity,
                @Nullable Vec3 damageSourcePosition,
                boolean skip
        ) {
            super(type, directEntity, causingEntity, damageSourcePosition);
            this.lastResult = lastResult;
            this.skip = skip;
        }

        public DamageResult getLastResult() {
            return lastResult;
        }

        public boolean canSkip() {
            return skip;
        }

        public void setSkip(boolean skip) {
            this.skip = skip;
        }
    }
}
