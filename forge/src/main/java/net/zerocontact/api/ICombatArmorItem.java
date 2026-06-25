package net.zerocontact.api;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface ICombatArmorItem {

    static int generateLossDefault(float damageAmount, float durabilityLossFactor, int hits) {
        return (int) Math.round(0.4 * Math.pow(damageAmount * durabilityLossFactor, 1.5) * (1 + hits * 0.1f));
    }

    default int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return (int) Math.ceil(0.4 * Math.pow(damageAmount * durabilityLossFactor, 1.5) * (1 + hits * 0.1f));
    }


    default float generateRicochet() {
        return 0.1f;
    }


    default float generatePenetrated() {
        return 0.7f;
    }


    default float generateBlunt() {
        return 0.35f;
    }


    default int getAbsorb() {
        return 0;
    }


    default int getDefense() {
        return 0;
    }


    default float getMass() {
        return 0;
    }

    Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack);

    default void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (stack.getItem() instanceof IEquipmentTypeTag tag) {
            Component armorCategoryLabel = Component.translatable("tooltip.zerocontact.armor_category")
                    .append(": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(
                            Component.translatable(tag.getArmorType().getTranslationValue())
                                    .withStyle(ChatFormatting.YELLOW)
                    );
            tooltipComponents.add(armorCategoryLabel);
        }
        if (stack.getItem() instanceof ICombatArmorItem combatArmorItem) {
            Function<Float, Integer> decimalToPercentOff = (amount) -> (int) Math.round((1 - amount) * 100);
            Component protectionLevel = Component.translatable("tooltip.zerocontact.armor_protection")
                    .append(":")
                    .withStyle(ChatFormatting.GOLD)
                    .append(
                            Component.literal(String.valueOf((combatArmorItem.getAbsorb())))
                                    .withStyle(ChatFormatting.YELLOW)
                    );
            Component armorBluntReduction = Component.translatable("tooltip.zerocontact.armor_blunt_reduction")
                    .append(": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(
                            Component.literal(String.valueOf(decimalToPercentOff.apply(combatArmorItem.generateBlunt())))
                                    .append("%")
                                    .withStyle(ChatFormatting.YELLOW)
                    );
            Component armorPenetrateReduction = Component.translatable("tooltip.zerocontact.armor_penetrate_reduction")
                    .append(": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(
                            Component.literal(String.valueOf(decimalToPercentOff.apply(combatArmorItem.generatePenetrated())))
                                    .append("%")
                                    .withStyle(ChatFormatting.YELLOW)
                    );
            tooltipComponents.addAll(List.of(protectionLevel, armorBluntReduction, armorPenetrateReduction));
        }
    }
}

