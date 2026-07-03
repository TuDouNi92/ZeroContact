package net.zerocontact.events;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.entity.EntityKineticBullet;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AmmoInjector {

    public record AmmoContext(CaliberVariantDamageHelper.Caliber caliber) {
    }

    private static final Map<UUID, AmmoContext> mapping = new HashMap<>();

    //Write for data-driven registration
    public static void write(AmmoContext context, ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        CaliberVariantDamageHelper.Caliber caliber = context.caliber;
        tag.putString("ai_ammoId", caliber.id());
        tag.putString("variant", caliber.variant());
        tag.putFloat("ai_damageFactor", caliber.baseDamageFactor());
        tag.putInt("ai_penetrate_level", caliber.penetrationClass());
        tag.putFloat("ai_flesh_damage", caliber.fleshDamage());
        tag.putFloat("ai_armor_damage", caliber.armorDamage());
        tag.putInt("stack_size", caliber.stackSize());
        tag.putIntArray("tracer_color", caliber.tracerColor());
        stack.getOrCreateTag().put("ai_ammo", tag);
    }


    //Read and bind Bullet and Gun in the spawn event
    public static AmmoContext read(ItemStack stack) {
        if (stack.getTag() == null) return null;
        CompoundTag tag = stack.getTag().getCompound("ai_ammo");
        String id = tag.getString("ai_ammoId");
        String variant = tag.getString("existed_variant");
        float damageFactor = tag.getFloat("ai_damageFactor");
        int level = tag.getInt("ai_penetrate_level");
        float flesh = tag.getFloat("ai_flesh_damage");
        float armorDamage = tag.getFloat("ai_armor_damage");
        int stackSize = tag.getInt("stack_size");
        int[] tracerColor = tag.getIntArray("tracer_color");
        return new AmmoContext(new CaliberVariantDamageHelper.Caliber(id, variant, damageFactor, level, flesh, armorDamage, stackSize, tracerColor));
    }

    //Sync tags when change cartridge;
    private static void copyTags(CaliberVariantDamageHelper.Caliber defaultCaliber, ItemStack gun) {
        String id = defaultCaliber.id();
        float damageFactor = defaultCaliber.baseDamageFactor();
        String variant = defaultCaliber.variant();
        int level = defaultCaliber.penetrationClass();
        float flesh = defaultCaliber.fleshDamage();
        float armorDamage = defaultCaliber.armorDamage();
        int stackSize = defaultCaliber.stackSize();
        int[] tracerColor = defaultCaliber.tracerColor();
        CompoundTag gunTag = gun.getOrCreateTagElement("ai_ammo");
        gunTag.putString("ai_ammoId", id);
        gunTag.putString("variant", variant);
        gunTag.putFloat("ai_damageFactor", damageFactor);
        gunTag.putInt("ai_penetrate_level", level);
        gunTag.putFloat("ai_flesh_damage", flesh);
        gunTag.putFloat("ai_armor_damage", armorDamage);
        gunTag.putInt("stack_size", stackSize);
        gunTag.putIntArray("tracer_color", tracerColor);
    }

    //Bind in spawn
    public static void bind(EntityKineticBullet bullet, AmmoContext context) {
        mapping.put(bullet.getUUID(), context);
    }

    public static @Nullable AmmoContext get(EntityKineticBullet bullet) {
        return mapping.get(bullet.getUUID());
    }

    //Consume in leave event
    public static void consume(EntityKineticBullet bullet) {
        mapping.remove(bullet.getUUID());
    }

    //Get cartridge for held gun
    public static String getAmmoVariantInGun(ItemStack gunStack) {
        return gunStack.getOrCreateTagElement("ai_ammo").getString("existed_variant");
    }

    //Get generated cartridge stack;
    public static ItemStack getDefaultStack(String fullKey) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(fullKey));
        if (item instanceof GenerateAmmo ammo) {
            return ammo.getDefaultInstance();
        }
        return AmmoItemBuilder.create().build();
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
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", defaultVariant);
        return new AmmoContext(new CaliberVariantDamageHelper.Caliber(defaultAmmo.toString(), defaultVariant, 0, 0, 0, 0, 0, new int[]{255, 255, 255, 255}));
    }

    private static void setDefaultAmmoVariantInGun(ItemStack gunStack, ResourceLocation ammoKey) {
        ResourceLocation defaultAmmo = getGunDefaultAmmo(gunStack);
        if (defaultAmmo.toString().isEmpty()) return;
        if (ammoKey.toString().isEmpty()) return;
        gunStack.getOrCreateTagElement("ai_ammo").putString("ai_ammoId", defaultAmmo.toString());
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", ammoKey.toString());
    }

    //Update cartridge tag in gun
    public static void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        gunStack.getOrCreateTagElement("ai_ammo").putString("existed_variant", selectedVariant);
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(selectedVariant));
        if (!(ammoItem instanceof GenerateAmmo ammo)) return;
        CaliberVariantDamageHelper.Caliber caliber = ammo.getDefualtCaliber();
        copyTags(caliber, gunStack);
    }

    public static String getClientSelectedAmmoVariant(ItemStack gunStack) {
        return gunStack.getOrCreateTag().getCompound("ai_ammo").getString("selected_variant");
    }

    public static void setClientSelectedAmmoVariant(ItemStack gunStack, String selectedAmmoKey) {
        gunStack.getOrCreateTagElement("ai_ammo").putString("selected_variant", selectedAmmoKey);
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

    public static boolean isEmptyContext(AmmoContext context) {
        return context.caliber.id().isEmpty() || context.caliber.variant().isEmpty();
    }
}
