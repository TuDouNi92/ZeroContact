package net.zerocontact.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.events.PlateInteract;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public interface PlateInfoProvider extends ICurioItem, ProtectionInfoProvider {
    default ICurio.@NotNull SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(ModSoundEventsReg.ARMOR_EQUIP_PLATE, 1.5f, 1.0f);
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        stack.getOrCreateTag().putInt("protection_class", getAbsorb());
        modifiers.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor" + uuid).getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.nameUUIDFromBytes(("MoveSpeed" + uuid).getBytes()), "MoveSpeed", getMass(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.nameUUIDFromBytes(("KnockBack" + uuid).getBytes()), "KnockBack", .4f, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    default boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    default void curioTick(SlotContext slotContext, ItemStack stack) {
        PlateInteract.onArmorUnequip(slotContext, stack);
    }

    @NotNull
    default ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        if(slotContext.entity() instanceof ArmedRaider){
            RandomSource randomSource =RandomSource.create();
            if(randomSource.nextFloat()<=0.8F){
                return ICurio.DropRule.DESTROY;
            }
            return ICurioItem.super.getDropRule(slotContext, source, lootingLevel, recentlyHit, stack);
        }
        return ICurioItem.super.getDropRule(slotContext, source, lootingLevel, recentlyHit, stack);
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return false;
    }
}
