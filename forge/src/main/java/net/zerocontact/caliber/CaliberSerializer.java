package net.zerocontact.caliber;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class CaliberSerializer {
    public static CompoundTag save(AmmoInjector.AmmoContext context) {
        CaliberVariantDamageHelper.Caliber caliber = context.caliber();
        CompoundTag ammoTag = new CompoundTag();
        ammoTag.putString("ai_ammoId", caliber.id());
        ammoTag.putString("variant", caliber.variant());
        ammoTag.putFloat("ai_damageFactor", caliber.baseDamageFactor());
        ammoTag.putInt("ai_penetrate_level", caliber.penetrationClass());
        ammoTag.putFloat("ai_flesh_damage", caliber.fleshDamage());
        ammoTag.putFloat("ai_armor_damage", caliber.armorDamage());
        ammoTag.putInt("stack_size", caliber.stackSize());
        ammoTag.putIntArray("tracer_color", caliber.tracerColor());
        CompoundTag finalTag = new CompoundTag();
        finalTag.put("ai_ammo", ammoTag);
        return finalTag;
    }

    public static AmmoInjector.AmmoContext load(@Nullable CompoundTag tag) {
        CompoundTag ammoTag = new CompoundTag();
        if (tag != null) {
            ammoTag = tag.getCompound("ai_ammo");
        }
        String id = ammoTag.getString("ai_ammoId");
        String variant = ammoTag.getString("variant");
        float damageFactor = ammoTag.getFloat("ai_damageFactor");
        int level = ammoTag.getInt("ai_penetrate_level");
        float flesh = ammoTag.getFloat("ai_flesh_damage");
        float armorDamage = ammoTag.getFloat("ai_armor_damage");
        int stackSize = ammoTag.getInt("stack_size");
        int[] tracerColor = ammoTag.getIntArray("tracer_color");
        return new AmmoInjector.AmmoContext(
                new CaliberVariantDamageHelper.Caliber(
                        id,
                        variant,
                        damageFactor,
                        level,
                        flesh,
                        armorDamage,
                        stackSize,
                        tracerColor
                )
        );
    }
}
