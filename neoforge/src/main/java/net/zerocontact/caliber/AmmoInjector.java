package net.zerocontact.caliber;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.zerocontact.ZeroContact;
import net.zerocontact.caliber.api.AmmoInjectService;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.tacz.guns.api.item.IAmmo;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static net.zerocontact.caliber.CaliberSerializer.*;

public final class AmmoInjector implements AmmoInjectService {

    private static AmmoInjectService ammoInjectService;

    public static AmmoInjectService get() {
        return Objects.requireNonNullElseGet(ammoInjectService, AmmoInjector::new);
    }

    //Write for data-driven registration
    public void writeCxtToStack(AmmoContext context, ItemStack stack) {
        CompoundTag ammoTag = CaliberSerializer.save(context);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.merge(ammoTag));
    }


    //Read and bind Bullet and Gun in the spawn event
    public AmmoContext readCxtFromStack(ItemStack stack) {
        return CaliberSerializer.load(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), stack);
    }

    //Sync tags when change cartridge;
    public void syncTagWithCartridge(CaliberHelper.Caliber defaultCaliber, ItemStack gun) {
        writeCxtToStack(new AmmoContext(defaultCaliber), gun);
    }

    //Get default tacz ammoId for gun.
    public ResourceLocation getDefaultAmmoIDFromGun(ItemStack gunStack) {
        ResourceLocation defaultAmmoId = ResourceLocation.parse("");
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

    //Set default ammo for gun
    public @Nullable AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack) {
        String defaultVariant = DEFAULT_AMMO;
        ResourceLocation defaultAmmo = getDefaultAmmoIDFromGun(gunStack);
        if (defaultAmmo.toString().isEmpty()) return null;
        updateAmmoTag(gunStack, ammoTag -> {
            ammoTag.putString(AI_AMMO_ID, defaultAmmo.toString());
            ammoTag.putString(VARIANT, defaultVariant);
            ammoTag.putString(SELECTED_VARIANT, defaultVariant);
            ammoTag.putString(EXISTED_VARIANT, defaultVariant);
            ammoTag.putString(EXISTED_AMMO_ID, defaultAmmo.toString());
        });
        return readCxtFromStack(gunStack);
    }

    public @NotNull Item getItemForVariant(AmmoContext context) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(context.caliber().variant()));
    }

    //Update cartridge tag in gun

    public void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        //Empty selectedVariant indicates that the gunStack need to be initialized with default ammo.
        ResourceLocation selectedId = ResourceLocation.tryParse(selectedVariant);
        if (selectedId == null || selectedId.getPath().isEmpty()) {
            AmmoContext context = setDefaultAmmoVariantInGun(gunStack);
            if (context != null) {
                syncTagWithCartridge(context.caliber(), gunStack);
            }
            return;
        }
        Item ammoItem = BuiltInRegistries.ITEM.get(selectedId);
        if (ammoItem instanceof GenerateAmmo ammo) {
            CaliberHelper.Caliber caliber = ammo.getDefualtCaliber();
            syncTagWithCartridge(caliber, gunStack);

            updateAmmoTag(gunStack, ammoTag -> {
                ammoTag.putString(EXISTED_VARIANT, selectedVariant);
                ammoTag.putString(EXISTED_AMMO_ID, selectedVariant);
            });
            return;
        }
        if (ammoItem instanceof IAmmo) {
            // Do not use getDefaultInstance() to initialize an IAmmo stack because it has no ammoId! Fetch ammoId from GunData.
            @Nullable IGun iGun = IGun.getIGunOrNull(gunStack);
            if (iGun == null) return;
            ResourceLocation gunId = iGun.getGunId(gunStack);
            @Nullable var gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
            if (gunIndex == null) return;
            ResourceLocation ammoId = gunIndex.getGunData().getAmmoId();
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
        syncTagWithCartridge(context.caliber(), gunStack);
        String selectedAmmoId = selectedId.toString();
        updateAmmoTag(gunStack, ammoTag -> {
            ammoTag.putString(EXISTED_VARIANT, selectedVariant);
            ammoTag.putString(EXISTED_AMMO_ID, selectedAmmoId);
        });
    }

    private static void updateAmmoTag(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag ammoTag = tag.getCompound(AI_AMMO);
            updater.accept(ammoTag);
            tag.put(AI_AMMO, ammoTag);
        });
    }


    //Set default cartridge for entity
    public void prepareGunCtxForEntity(LivingEntity livingEntity) {
        ItemStack mainHandStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
        IGun gun = IGun.getIGunOrNull(mainHandStack);
        if (gun == null) return;
        setDefaultAmmoVariantInGun(mainHandStack);
    }

    //Use for changing the cartridges from custom entities
    public void setVariantForEntity(ItemStack gunStack, Item ammo) {
        if (ammo instanceof GenerateAmmo generateAmmo) {
            setAmmoVariantInGun(gunStack, ZeroContact.MOD_ID + ":" + generateAmmo);
        }
    }

    @Deprecated(forRemoval = true)
    //Correct the default context on mainHand in damage processing if it is somehow empty
    public @Nullable AmmoContext setPlayerGunContext(ServerPlayer player) {
        ItemStack checkGunStack = player.getMainHandItem();
        if (!IGun.mainHandHoldGun(player)) return null;
        return setDefaultAmmoVariantInGun(checkGunStack);
    }

    public String getAmmoIdentity(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        if (stack.getItem() instanceof GenerateAmmo) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return itemId.toString();
        }

        if (stack.getItem() instanceof IAmmo ammo) {
            ResourceLocation ammoId = ammo.getAmmoId(stack);
            return ammoId == null ? "" : ammoId.toString();
        }

        return "";
    }

}
