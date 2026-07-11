package net.zerocontact.capability;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberSerializer;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.item.ammo.GenerateAmmo;

import java.util.Optional;

public class GunCartridgeTypeCap implements ICartridgeHolder {
    //Sync tags when change cartridge;
    public void copyTags(CaliberVariantDamageHelper.Caliber defaultCaliber, ItemStack gun) {
        gun.getOrCreateTag().merge(CaliberSerializer.save(new AmmoInjector.AmmoContext(defaultCaliber)));
    }


    //Get cartridge for held gun
    public String getAmmoVariantInGun(ItemStack gunStack) {
        return gunStack.getOrCreateTagElement("ai_ammo").getString("existed_variant");
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
        ResourceLocation defaultAmmoId = new ResourceLocation("");
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return defaultAmmoId;
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack));
        if (gunIndex.isPresent()) {
            CommonGunIndex gunIndex1 = gunIndex.get();
            GunData gunData = gunIndex1.getGunData();
            defaultAmmoId = gunData.getAmmoId();
        }
        return defaultAmmoId;
    }


    public void setDefaultAmmoVariantInGun(ItemStack gunStack, ResourceLocation ammoKey) {
        ResourceLocation defaultAmmo = getGunDefaultAmmo(gunStack);
        if (defaultAmmo.toString().isEmpty()) return;
        if (ammoKey.toString().isEmpty()) return;
        gunStack.getOrCreateTagElement("ai_ammo").putString("ai_ammoId", defaultAmmo.toString());
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", ammoKey.toString());
    }

    //Update cartridge tag in gun
    public void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", selectedVariant);
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(selectedVariant));
        if (!(ammoItem instanceof GenerateAmmo ammo)) return;
        CaliberVariantDamageHelper.Caliber caliber = ammo.getDefualtCaliber();
        copyTags(caliber, gunStack);
    }

    public String getClientSelectedAmmoVariant(ItemStack gunStack) {
        return gunStack.getOrCreateTag().getCompound("ai_ammo").getString("selected_variant");
    }

    public void setClientSelectedAmmoVariant(ItemStack gunStack, String selectedAmmoKey) {
        gunStack.getOrCreateTagElement("ai_ammo").putString("selected_variant", selectedAmmoKey);
    }
}
