package net.zerocontact.entity;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.events.AmmoInjector;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

class LoadoutHolder {
    public static final String DEFAULT_ARMOR = "zerocontact:armor_jpc_v1";
    public static final String DEFAULT_PLATE = "zerocontact:plate_steel";
    public static final String DEFAULT_HELMET = "zerocontact:helmet_tbh_iiia";
    public static final String DEFAULT_BACKPACK = "zerocontact:backpack_t20_umbra";
    public static final String DEFAULT_ARMBAND = "zerocontact:armband_red";
    public final String gunId;
    @Nullable
    public final String ammoId;
    public final String armorId;
    @Nullable
    public final String plateId;
    @Nullable
    public final String helmetId;
    @Nullable
    public final String backpackId;
    @Nullable
    public final String armbandId;

    public ItemStack
            gunStack = ItemStack.EMPTY,
            ammoStack = ItemStack.EMPTY,
            armorStack = ItemStack.EMPTY,
            plateStack = ItemStack.EMPTY,
            helmetStack = ItemStack.EMPTY,
            backpackStack = ItemStack.EMPTY,
            armbandStack = ItemStack.EMPTY;

    private LoadoutHolder(String gunId) {
        this.gunId = gunId;
        this.ammoId = null;
        this.armorId = DEFAULT_ARMOR;
        this.plateId = DEFAULT_PLATE;
        this.helmetId = DEFAULT_HELMET;
        this.backpackId = DEFAULT_BACKPACK;
        this.armbandId = DEFAULT_ARMBAND;
        finalizeNecessary();
    }

    private LoadoutHolder(String gunId, @Nullable String ammoId) {
        this.gunId = gunId;
        this.ammoId = ammoId;
        this.armorId = DEFAULT_ARMOR;
        this.plateId = DEFAULT_PLATE;
        this.helmetId = DEFAULT_HELMET;
        this.backpackId = DEFAULT_BACKPACK;
        this.armbandId = DEFAULT_ARMBAND;
        finalizeNecessary();
    }

    void finalizeNecessary() {
        finalizeGun();
        if (this.gunStack.isEmpty()) return;
        finalizeAmmo(ammoId);
    }


    public static LoadoutHolder create(@NotNull String gunId, @Nullable String ammoId) {
        return new LoadoutHolder(gunId, ammoId);
    }

    //Get a gun stack generated from TaCZ API
    void finalizeGun() {
        ItemStack gunStack = GunItemBuilder.create().setId(new ResourceLocation(gunId)).build();
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return;
        TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack)).ifPresent(commonGunIndex -> {
            GunData gunData = commonGunIndex.getGunData();
            gun.setFireMode(gunStack, gunData.getFireModeSet().get(0));
            gun.setCurrentAmmoCount(gunStack, gunData.getAmmoAmount());
        });
        this.gunStack = gunStack;
    }

    void finalizeAmmo(@Nullable String ammoId) {
        ItemStack ammoStack;
        IGun gun = IGun.getIGunOrNull(this.gunStack);
        if (gun == null) return;
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack));
        if (gunIndex.isPresent()) {
            GunData gunData = gunIndex.get().getGunData();
            ResourceLocation ammoResource = gunData.getAmmoId();
            Integer stackSize = TimelessAPI.getCommonAmmoIndex(ammoResource).map(CommonAmmoIndex::getStackSize).orElse(1);
            //If input is not vanilla ammo, find its resource and build. Or build the vanilla ammo
            if (ammoId != null && !ammoId.equals(ammoResource.toString())) {
                @Nullable Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ammoId));
                @Nullable GenerateAmmo generateAmmo = ammoItem instanceof GenerateAmmo ? (GenerateAmmo) ammoItem : null;
                if (generateAmmo != null) {
                    ammoStack = generateAmmo.getDefaultInstance();
                    if (!generateAmmo.isAmmoOfGun(gunStack, ammoStack)) return;
                    ammoStack.setCount(ammoStack.getMaxStackSize());
                    AmmoInjector.setEntityGunContext(gunStack, generateAmmo);
                    this.ammoStack = ammoStack;
                    return;
                }
            }
            this.ammoStack = AmmoItemBuilder.create().setId(ammoResource).setCount(stackSize).build();
        }
    }

    public LoadoutHolder setArmor(@Nullable String armorId) {
        this.armorStack = finalizeItem(armorId, DEFAULT_ARMOR);
        return this;
    }

    public LoadoutHolder setPlate(@Nullable String plateId) {
        this.plateStack = finalizeItem(plateId, DEFAULT_PLATE);
        return this;
    }

    public LoadoutHolder setHelmet(@Nullable String helmetId) {
        this.helmetStack = finalizeItem(helmetId, DEFAULT_HELMET);
        return this;
    }

    public LoadoutHolder setBackpack(@Nullable String backpackId) {
        this.backpackStack = finalizeItem(backpackId, DEFAULT_BACKPACK);
        return this;
    }

    public LoadoutHolder setArmband(@Nullable String armbandId) {
        this.armbandStack = finalizeItem(armbandId, DEFAULT_ARMBAND);
        return this;
    }

    public LoadoutData build() {
        return new LoadoutData(gunId, ammoId, gunStack, ammoStack, armorStack, plateStack, helmetStack, backpackStack, armbandStack);
    }

    private ItemStack finalizeItem(@Nullable String id, @Nullable String defaultId) {
        if (defaultId == null) return ItemStack.EMPTY;
        @Nullable Item defaultItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(defaultId));
        if (defaultItem == null) return ItemStack.EMPTY;
        ItemStack defaultStack = new ItemStack(defaultItem);
        if (id == null) return defaultStack;
        @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) return defaultStack;
        return new ItemStack(item);
    }

    record LoadoutData(
            String gunId,
            String ammoId,
            ItemStack gunStack,
            ItemStack ammoStack,
            ItemStack armorStack,
            ItemStack plateStack,
            ItemStack helmetStack,
            ItemStack backpackStack,
            ItemStack armbandStack
    ) {
    }

    enum LoadoutTypes {
        AK(new LoadoutHolder("tacz:ak47")),
        SKS(new LoadoutHolder("tacz:sks_tactical")),
        FN_FAL(new LoadoutHolder("tacz:fn_fal")),
        M4(new LoadoutHolder("tacz:m4a1")),
        SCAR_L(new LoadoutHolder("tacz:scar_l")),
        M320(new LoadoutHolder("tacz:m320")),
        AUG(new LoadoutHolder("tacz:aug")),
        HK416D(new LoadoutHolder("tacz:hk416d")),
        M16A4(new LoadoutHolder("tacz:m16a4")),
        QBZ191(new LoadoutHolder("tacz:qbz_191")),
        CZ75(new LoadoutHolder("tacz:cz75")),
        GLOCK17(new LoadoutHolder("tacz:glock_17")),
        P320(new LoadoutHolder("tacz:p320")),
        P90(new LoadoutHolder("tacz:p90")),
        VECTOR(new LoadoutHolder("tacz:vector45")),
        UZI(new LoadoutHolder("tacz:uzi")),
        M700(new LoadoutHolder("tacz:m700")),
        M249(new LoadoutHolder("tacz:m249"));
        public final LoadoutHolder loadoutHolder;
        public final ItemStack gunStack;
        public final ItemStack ammoStack;

        LoadoutTypes(LoadoutHolder loadoutHolder) {
            this.loadoutHolder = loadoutHolder;
            gunStack = loadoutHolder.gunStack;
            ammoStack = loadoutHolder.ammoStack;
        }
    }
}
