package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import org.jetbrains.annotations.Nullable;

public interface ICartridgeHolder {
    void copyTags(CaliberVariantDamageHelper.Caliber defaultCaliber, ItemStack gun);

    //Get cartridge for held gun
    String getAmmoVariantInGun(ItemStack gunStack);

    //Get generated cartridge stack;
    ItemStack getDefaultStack(String fullKey);

    //Get default tacz ammoId for gun.
    ResourceLocation getGunDefaultAmmo(ItemStack gunStack);

    @Nullable AmmoInjector.AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack);

    //Update cartridge tag in gun
    void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant);

    String getClientSelectedAmmoVariant(ItemStack gunStack);

    void setClientSelectedAmmoVariant(ItemStack gunStack, String selectedAmmoKey);
}
