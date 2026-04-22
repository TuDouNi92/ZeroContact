package net.zerocontact.api;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public interface ICombatArmorItem {


    default int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return (int) Math.round(0.4 * Math.pow(damageAmount * durabilityLossFactor, 1.5) * (1 + hits * 0.1f));
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
}
