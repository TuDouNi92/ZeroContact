package net.zerocontact.caliber;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AmmoInjector {

    public record AmmoContext(CaliberVariantDamageHelper.Caliber caliber) {
        public boolean isEmpty() {
            return caliber.id().isEmpty() || caliber.variant().isEmpty();
        }
    }

    //Write for data-driven registration
    public static void write(AmmoContext context, ItemStack stack) {
        CompoundTag ammoTag = CaliberSerializer.save(context);
        stack.getOrCreateTag().merge(ammoTag);
    }


    //Read and bind Bullet and Gun in the spawn event
    public static AmmoContext read(ItemStack stack) {
        return CaliberSerializer.load(stack.getTag());
    }

    //Sync tags when change cartridge;
    private static void copyTags(CaliberVariantDamageHelper.Caliber defaultCaliber, ItemStack gun) {
        gun.getOrCreateTag().merge(CaliberSerializer.save(new AmmoContext(defaultCaliber)));
    }

    //Get default tacz ammoId for gun.
    private static ResourceLocation getGunDefaultAmmo(ItemStack gunStack) {
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

    //Set default ammo in gun once spawn
    private static @Nullable AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack) {
        String defaultVariant = "tacz:ammo";
        ResourceLocation defaultAmmo = getGunDefaultAmmo(gunStack);
        if (defaultAmmo.toString().isEmpty()) return null;
        gunStack.getOrCreateTagElement("ai_ammo").putString("ai_ammoId", defaultAmmo.toString());
        gunStack.getOrCreateTagElement("ai_ammo").putString("variant",defaultVariant);
        gunStack.getOrCreateTagElement("ai_ammo").putString("selected_variant", defaultVariant);
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", defaultVariant);
        return new AmmoContext(new CaliberVariantDamageHelper.Caliber(defaultAmmo.toString(), defaultVariant, 0, 0, 0, 0, 0, new int[]{255, 255, 255, 255}));
    }

    public static @Nullable Item getAmmoVariantItem(AmmoContext context) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(context.caliber.variant()));
    }

    //Update cartridge tag in gun
    public static void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", selectedVariant);
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(selectedVariant));
        if (!(ammoItem instanceof GenerateAmmo ammo)) {
            AmmoContext context = setDefaultAmmoVariantInGun(gunStack);
            if (context == null) return;
            copyTags(context.caliber(), gunStack);
            return;
        }
        CaliberVariantDamageHelper.Caliber caliber = ammo.getDefualtCaliber();
        copyTags(caliber, gunStack);
    }


    //Set default cartridge for entity
    public static void setEntityGunContext(ItemStack gunStack) {
        setDefaultAmmoVariantInGun(gunStack);
    }

    //Use for changing the cartridges from custom entities
    public static void setEntityGunContext(ItemStack gunStack, Item ammo) {
        if (ammo instanceof GenerateAmmo generateAmmo) {
            setAmmoVariantInGun(gunStack, ZeroContact.MOD_ID + ":" + generateAmmo);
        }
    }

    //Correct the default context on mainHand in damage processing if it is somehow empty
    public static @Nullable AmmoContext setPlayerGunContext(ServerPlayer player) {
        ItemStack checkGunStack = player.getMainHandItem();
        if (!IGun.mainHandHoldGun(player)) return null;
        return setDefaultAmmoVariantInGun(checkGunStack);
    }
}
