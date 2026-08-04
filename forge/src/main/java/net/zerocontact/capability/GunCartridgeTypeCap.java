package net.zerocontact.capability;

import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberSerializer;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.zerocontact.caliber.CaliberSerializer.*;

public class GunCartridgeTypeCap implements ICartridgeHolder {
    //Sync tags when change cartridge;
    public void copyTags(CaliberVariantDamageHelper.Caliber defaultCaliber, ItemStack gun) {
        AmmoInjector.copyTags(defaultCaliber, gun);
    }


    //Get cartridge for held gun
    public String getAmmoVariantInGun(ItemStack gunStack) {
        return gunStack.getOrCreateTagElement(AI_AMMO).getString(EXISTED_VARIANT);
    }

    //Get generated cartridge stack;
    public ItemStack getDefaultStack(String fullKey) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(fullKey));
        if (item instanceof GenerateAmmo ammo) {
            return ammo.getDefaultInstance();
        }
        return AmmoItemBuilder.create().build();
    }

    //Get default tacz ammoId for gun.
    public ResourceLocation getGunDefaultAmmo(ItemStack gunStack) {
        return AmmoInjector.getGunDefaultAmmo(gunStack);
    }


    public @Nullable AmmoInjector.AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack) {
        return AmmoInjector.setDefaultAmmoVariantInGun(gunStack);
    }

    //Update cartridge tag in gun
    public void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        AmmoInjector.setAmmoVariantInGun(gunStack, selectedVariant);
    }

    public String getClientSelectedAmmoVariant(ItemStack gunStack) {
        return gunStack.getOrCreateTag().getCompound(AI_AMMO).getString(SELECTED_VARIANT);
    }

    public void setClientSelectedAmmoVariant(ItemStack gunStack, String selectedAmmoKey) {
        gunStack.getOrCreateTagElement(AI_AMMO).putString(SELECTED_VARIANT, selectedAmmoKey);
    }

    @Override
    public @Nullable GunRecoil getRecoil(ItemStack gunStack) {
        AmmoInjector.AmmoContext context = CaliberSerializer.load(gunStack.getTag(), gunStack);
        if (context.isEmpty()) return null;
        return context.caliber().getRecoil(gunStack);
    }

    @Override
    public @NotNull Map<InaccuracyType, Float> getInaccuracy(ItemStack gunStack) {
        AmmoInjector.AmmoContext context = CaliberSerializer.load(gunStack.getTag(), gunStack);
        if (context.isEmpty()) return Map.of();
        return context.caliber().getInaccuracy(gunStack);
    }
}
