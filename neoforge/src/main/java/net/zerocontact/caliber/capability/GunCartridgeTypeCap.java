package net.zerocontact.caliber.capability;

import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.zerocontact.caliber.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberSerializer;
import net.zerocontact.caliber.CaliberHelper;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.zerocontact.caliber.CaliberSerializer.*;

public class GunCartridgeTypeCap implements ICartridgeHolder {
    private boolean creativeHandling = false;

    //Sync tags when change cartridge;
    public void copyTags(CaliberHelper.Caliber defaultCaliber, ItemStack gun) {
        AmmoInjector.get().syncTagWithCartridge(defaultCaliber, gun);
    }


    //Get cartridge for held gun
    public String getAmmoVariantInGun(ItemStack gunStack) {
        return gunStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getCompound(AI_AMMO).getString(EXISTED_VARIANT);
    }

    //Get generated cartridge stack;
    public ItemStack getDefaultStack(String fullKey) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(fullKey));
        if(item == Items.AIR)return item.getDefaultInstance();
        if (item instanceof GenerateAmmo ammo) {
            return ammo.getDefaultInstance();
        }
        return AmmoItemBuilder.create().build();
    }

    //Get default tacz ammoId for gun.
    public ResourceLocation getGunDefaultAmmo(ItemStack gunStack) {
        return AmmoInjector.get().getDefaultAmmoIDFromGun(gunStack);
    }


    public @Nullable AmmoInjector.AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack) {
        return AmmoInjector.get().setDefaultAmmoVariantInGun(gunStack);
    }

    //Update cartridge tag in gun
    public void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        AmmoInjector.get().setAmmoVariantInGun(gunStack, selectedVariant);
    }

    public String getClientSelectedAmmoVariant(ItemStack gunStack) {
        return gunStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getCompound(AI_AMMO).getString(SELECTED_VARIANT);
    }

    public void setClientSelectedAmmoVariant(ItemStack gunStack, String selectedAmmoKey) {
        CustomData.update(DataComponents.CUSTOM_DATA, gunStack, tag -> {
            CompoundTag ammoTag = tag.getCompound(AI_AMMO);
            ammoTag.putString(SELECTED_VARIANT, selectedAmmoKey);
            tag.put(AI_AMMO, ammoTag);
        });
    }

    @Override
    public @Nullable GunRecoil getRecoil(ItemStack gunStack) {
        AmmoInjector.AmmoContext context = CaliberSerializer.load(
                gunStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), gunStack);
        if (context.isEmpty()) return null;
        return context.caliber().getRecoil(gunStack);
    }

    @Override
    public @NotNull Map<InaccuracyType, Float> getInaccuracy(ItemStack gunStack) {
        AmmoInjector.AmmoContext context = CaliberSerializer.load(
                gunStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), gunStack);
        if (context.isEmpty()) return Map.of();
        return context.caliber().getInaccuracy(gunStack);
    }

    @Override
    public void setCreativeHandling(boolean creative) {
        creativeHandling = creative;
    }

    @Override
    public boolean getCreativeHandling() {
        boolean result = creativeHandling;
        creativeHandling = false;
        return result;
    }
}
