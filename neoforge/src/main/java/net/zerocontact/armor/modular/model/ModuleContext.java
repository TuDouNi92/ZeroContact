package net.zerocontact.armor.modular.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ModuleContext(
        Player wearer,
        EquipmentTarget equipmentTarget,
        ResourceLocation mountId,
        ItemStack module
) {
}
