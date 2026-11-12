package net.zerocontact.api.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.events.ArmorUnEquippedHelper;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public interface PlateInfoProvider extends ICurioItem, ICombatArmorItem {
    default ICurio.@NotNull SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(ModSoundEventsReg.ARMOR_EQUIP_PLATE, 1.5f, 1.0f);
    }


    default Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("protection_class", getAbsorb());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        modifiers.put(Attributes.ARMOR, new AttributeModifier(ResourceLocation.parse(String.valueOf(UUID.nameUUIDFromBytes(("Armor").getBytes()))), this.getDefense(), AttributeModifier.Operation.ADD_VALUE));
        modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.parse(String.valueOf(UUID.nameUUIDFromBytes(("MoveSpeed").getBytes()))), getMass(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ResourceLocation.parse(String.valueOf(UUID.nameUUIDFromBytes(("KnockBack").getBytes()))), .4f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        return modifiers;
    }

    default boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }


    default void curioTick(SlotContext slotContext, ItemStack stack) {
        ArmorUnEquippedHelper.onArmorUnequipped(slotContext, stack);
    }

    @Override
    @NotNull
    default ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit, ItemStack stack) {
        if (slotContext.entity() instanceof ArmedRaider) {
            RandomSource randomSource = RandomSource.create();
            if (randomSource.nextFloat() <= 0.8F) {
                return ICurio.DropRule.DESTROY;
            }
            return ICurioItem.super.getDropRule(slotContext, source, recentlyHit, stack);
        }
        return ICurioItem.super.getDropRule(slotContext, source, recentlyHit, stack);
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return false;
    }

    @Override
    default void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        ICombatArmorItem.super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }
}
