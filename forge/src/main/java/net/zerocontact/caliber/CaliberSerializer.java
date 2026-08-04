package net.zerocontact.caliber;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CaliberSerializer {
    public static final String DEFAULT_AMMO = "tacz:ammo";
    public static final String AI_AMMO_ID = "ai_ammoId";
    public static final String VARIANT = "variant";
    public static final String AI_AMMO = "ai_ammo";
    public static final String SELECTED_VARIANT = "selected_variant";
    public static final String EXISTED_VARIANT = "existed_variant";


    public static CompoundTag save(AmmoInjector.AmmoContext context) {
        CaliberVariantDamageHelper.Caliber caliber = context.caliber();
        CompoundTag ammoTag = new CompoundTag();
        ammoTag.putString(AI_AMMO_ID, caliber.id());
        ammoTag.putString(VARIANT, caliber.variant());
        CompoundTag finalTag = new CompoundTag();
        finalTag.put(AI_AMMO, ammoTag);
        return finalTag;
    }

    public static AmmoInjector.AmmoContext load(@Nullable CompoundTag tag, ItemStack gunStack) {
        CompoundTag ammoTag = new CompoundTag();
        if (tag != null) {
            ammoTag = tag.getCompound(AI_AMMO);
        }
        String id = ammoTag.getString(AI_AMMO_ID);
        String variant = ammoTag.getString(VARIANT);
        return CaliberRegistry.get(id, variant)
                .map(AmmoInjector.AmmoContext::new)
                .orElse(new AmmoInjector.AmmoContext(CaliberVariantDamageHelper.Caliber.createDefaultCaliberFromGun(id, gunStack)));
    }
}
