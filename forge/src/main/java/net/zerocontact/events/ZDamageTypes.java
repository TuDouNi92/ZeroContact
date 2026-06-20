package net.zerocontact.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


import static net.zerocontact.ZeroContact.MOD_ID;

public class ZDamageTypes {
    public static final ResourceKey<DamageType> ZC_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "zc_damage"));

    public static DamageSource create(Level level, Entity directEntity, Entity causingEntity, Vec3 sourcePosition) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ZDamageTypes.ZC_DAMAGE),
                directEntity,
                causingEntity,
                sourcePosition
        );
    }
}
