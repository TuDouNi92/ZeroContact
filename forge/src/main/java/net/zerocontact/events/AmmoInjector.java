package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AmmoInjector {

    public record AmmoContext(CaliberVariantDamageHelper.Caliber caliber) {
    }

    private static final Map<UUID, AmmoContext> mapping = new HashMap<>();


    //Write for data-driven needs
    public static void write(AmmoContext context, ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        CaliberVariantDamageHelper.Caliber caliber = context.caliber;
        tag.putString("ai_ammoId", caliber.id());
        tag.putString("variant", caliber.variant());
        tag.putFloat("ai_damageFactor", caliber.baseDamageFactor());
        tag.putInt("ai_penetrate_level", caliber.penetrationClass());
        tag.putFloat("ai_flesh_damage", caliber.fleshDamage());
        tag.putInt("stack_size", caliber.stackSize());
        stack.getOrCreateTag().put("ai_ammo", tag);
    }

    public static void write(AmmoContext context, CompoundTag tag) {
        CompoundTag subTag = new CompoundTag();
        CaliberVariantDamageHelper.Caliber caliber = context.caliber;
        subTag.putString("ai_ammoId", caliber.id());
        subTag.putString("variant", caliber.variant());
        subTag.putFloat("ai_damageFactor", caliber.baseDamageFactor());
        subTag.putInt("ai_penetrate_level", caliber.penetrationClass());
        subTag.putFloat("ai_flesh_damage", caliber.fleshDamage());
        subTag.putInt("stack_size", caliber.stackSize());
        tag.put("ai_ammo", subTag);
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
        int stackSize = tag.getInt("stack_size");
        return new AmmoContext(new CaliberVariantDamageHelper.Caliber(id, variant, damageFactor, level, flesh, stackSize));
    }


    public static void copyTags(ItemStack left, ItemStack right) {
        if (left.getTag() == null) return;
        CompoundTag ammoTag = left.getTag().getCompound("ai_ammo");
        right.getOrCreateTag().put("ai_ammo", ammoTag.copy());
    }

    //Bind in spawn
    public static void bind(EntityKineticBullet bullet, AmmoContext context) {
        mapping.put(bullet.getUUID(), context);
    }

    //Consume in hurt event
    public static AmmoContext get(EntityKineticBullet bullet) {
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
    }

    public static String getClientSelectedAmmoVariant(ItemStack gunStack) {
        return gunStack.getOrCreateTag().getCompound("ai_ammo").getString("selected_variant");
    }

    public static void setClientSelectedAmmoVariant(ItemStack gunStack, String selectedAmmoKey) {
        gunStack.getOrCreateTagElement("ai_ammo").putString("selected_variant", selectedAmmoKey);
    }
}
