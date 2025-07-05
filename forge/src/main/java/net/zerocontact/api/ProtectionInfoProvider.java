package net.zerocontact.api;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public interface ProtectionInfoProvider {
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
