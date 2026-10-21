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
import com.tacz.guns.api.item.IAmmo;
import java.util.Optional;

import static net.zerocontact.caliber.CaliberSerializer.*;

public class AmmoInjector {

    public record AmmoContext(CaliberHelper.Caliber caliber) {
        public boolean isEmpty() {
            return caliber == null || caliber.id().isEmpty() || caliber.variant().isEmpty();
        }
    }

    //Write for data-driven registration
    public static void write(AmmoContext context, ItemStack stack) {
        CompoundTag ammoTag = CaliberSerializer.save(context);
        stack.getOrCreateTag().merge(ammoTag);
    }


    //Read and bind Bullet and Gun in the spawn event
    public static AmmoContext read(ItemStack stack) {
        return CaliberSerializer.load(stack.getTag(), stack);
    }

    //Sync tags when change cartridge;
    public static void copyTags(CaliberHelper.Caliber defaultCaliber, ItemStack gun) {
        gun.getOrCreateTag().merge(CaliberSerializer.save(new AmmoContext(defaultCaliber)));
    }

    //Get default tacz ammoId for gun.
    public static ResourceLocation getGunDefaultAmmo(ItemStack gunStack) {
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
    public static @Nullable AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack) {
        String defaultVariant = DEFAULT_AMMO;
        ResourceLocation defaultAmmo = getGunDefaultAmmo(gunStack);
        if (defaultAmmo.toString().isEmpty()) return null;
        gunStack.getOrCreateTagElement(AI_AMMO).putString(AI_AMMO_ID, defaultAmmo.toString());
        gunStack.getOrCreateTagElement(AI_AMMO).putString(VARIANT, defaultVariant);
        gunStack.getOrCreateTagElement(AI_AMMO).putString(SELECTED_VARIANT, defaultVariant);
        gunStack.getOrCreateTagElement(AI_AMMO).putString(EXISTED_VARIANT, defaultVariant);
        gunStack.getOrCreateTagElement(AI_AMMO).putString(EXISTED_AMMO_ID, defaultAmmo.toString());
        return CaliberSerializer.load(gunStack.getTag(), gunStack);
    }

    public static @Nullable Item getAmmoVariantItem(AmmoContext context) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(context.caliber.variant()));
    }

    //Update cartridge tag in gun

    public static void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        ResourceLocation selectedId = ResourceLocation.tryParse(selectedVariant);
        if (selectedId == null) {
            return;
        }
        Item ammoItem = ForgeRegistries.ITEMS.getValue(selectedId);
        if (ammoItem instanceof GenerateAmmo ammo) {
            CaliberHelper.Caliber caliber = ammo.getDefualtCaliber();
            copyTags(caliber, gunStack);

            gunStack.getOrCreateTagElement(AI_AMMO)
                    .putString(EXISTED_VARIANT, selectedVariant);
            gunStack.getOrCreateTagElement(AI_AMMO)
                    .putString(EXISTED_AMMO_ID, selectedVariant);
            return;
        }
        if (ammoItem instanceof IAmmo ammo) {
            ItemStack ammoStack = ammoItem.getDefaultInstance();
            ResourceLocation ammoId = ammo.getAmmoId(ammoStack);
            if (ammoId == null) {
                return;
            }
            selectedId = ammoId;
        }
        // For data-driven TaCZ ammo, selectedId is the ammo ID, not an item ID.
        AmmoContext context = new AmmoContext(
                CaliberHelper.Caliber.createDefaultCaliberFromStack(
                        selectedId.toString(), gunStack
                )
        );
        if (context.isEmpty()) {
            return;
        }
        copyTags(context.caliber(), gunStack);
        gunStack.getOrCreateTagElement(AI_AMMO)
                .putString(EXISTED_VARIANT, selectedVariant);
        gunStack.getOrCreateTagElement(AI_AMMO)
                .putString(EXISTED_AMMO_ID, selectedId.toString());
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

    public static String getAmmoIdentity(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        if (stack.getItem() instanceof GenerateAmmo) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return itemId == null ? "" : itemId.toString();
        }

        if (stack.getItem() instanceof IAmmo ammo) {
            ResourceLocation ammoId = ammo.getAmmoId(stack);
            return ammoId == null ? "" : ammoId.toString();
        }

        return "";
    }

    public static void setExistedAmmoIdentity(ItemStack gunStack, String ammoId) {
        gunStack.getOrCreateTagElement(AI_AMMO)
                .putString(EXISTED_AMMO_ID, ammoId);
    }
}
