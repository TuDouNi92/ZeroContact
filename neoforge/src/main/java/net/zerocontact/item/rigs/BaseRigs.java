package net.zerocontact.item.rigs;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.item.backpack.BaseBackpack;
import org.jetbrains.annotations.NotNull;

public class BaseRigs extends BaseBackpack {
    public BaseRigs(ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super(texture, model, animation, containerSize);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.RIGS;
    }

    public void manualSave(ItemStack rigsStack, ItemStackHandler handler, HolderLookup.Provider provider) {
        CustomData.update(DataComponents.CUSTOM_DATA, rigsStack, compoundTag -> compoundTag.put("inventory", handler.serializeNBT(provider).getList("Items", Tag.TAG_COMPOUND)));
    }
}
