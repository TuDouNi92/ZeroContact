package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
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
        return new AmmoContext(new CaliberVariantDamageHelper.Caliber(id, variant, damageFactor, level, flesh, armorDamage, stackSize));
    }


    private static void copyTags(CaliberVariantDamageHelper.Caliber defaultCaliber, ItemStack gun) {
        String id = defaultCaliber.id();
        float damageFactor = defaultCaliber.baseDamageFactor();
        String variant = defaultCaliber.variant();
        int level = defaultCaliber.penetrationClass();
        float flesh = defaultCaliber.fleshDamage();
        float armorDamage = defaultCaliber.armorDamage();
        int stackSize = defaultCaliber.stackSize();
        CompoundTag gunTag = gun.getOrCreateTagElement("ai_ammo");
        gunTag.putString("ai_ammoId", id);
        gunTag.putString("variant", variant);
        gunTag.putFloat("ai_damageFactor", damageFactor);
        gunTag.putInt("ai_penetrate_level", level);
        gunTag.putFloat("ai_flesh_damage", flesh);
        gunTag.putFloat("ai_armor_damage", armorDamage);
        gunTag.putInt("stack_size", stackSize);
    }

    //Bind in spawn
    public static void bind(EntityKineticBullet bullet, AmmoContext context) {
        mapping.put(bullet.getUUID(), context);
    }

    //Consume in hurt event
    public static @Nullable AmmoContext get(EntityKineticBullet bullet) {
        return mapping.get(bullet.getUUID());
    }

    //Consume in hurt event
    public static void consume(EntityKineticBullet bullet) {
        mapping.remove(bullet.getUUID());
    }

    public static String getAmmoVariantInGun(ItemStack gunStack) {
        return gunStack.getOrCreateTagElement("ai_ammo").getString("existed_variant");
    }

    public static void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant) {
        gunStack.getOrCreateTag().getCompound("ai_ammo").putString("existed_variant", selectedVariant);
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
}
