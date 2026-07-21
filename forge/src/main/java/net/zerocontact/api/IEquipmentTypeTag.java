package net.zerocontact.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IEquipmentTypeTag {
    enum EquipmentType {
        PLATE_CARRIER("PLATE_CARRIER"),
        ARMOR("ARMOR"),
        HELMET("HELMET"),
        UNIFORM_TOP("UNIFORM_TOP"),
        UNIFORM_PANTS("UNIFORM_PANTS"),
        ARMBAND("ARMBAND"),
        HEADSET("HEADSET"),
        BACKPACK("BACKPACK"),
        RIGS("RIGS"),
        PLATE("PLATE"),
        AMMO("AMMO");
        private final String name;
        private final String translationValue;
        EquipmentType(String name) {
            this.name = name;
            this.translationValue = "tooltip.zerocontact.armor_category"+"."+name.toLowerCase();
        }

        public String getTypeId() {
            return name;
        }

        public String getTranslationValue() {
            return translationValue;
        }
    }

    default @NotNull IEquipmentTypeTag.EquipmentType getArmorType(){
        return EquipmentType.ARMOR;
    }

    default void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if(stack.getItem() instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(EquipmentType.RIGS)){
            Component rigTip = Component.translatable("tooltip.zerocontact.rig").withStyle(ChatFormatting.DARK_GRAY);
            tooltipComponents.add(rigTip);
        }
    }
}
