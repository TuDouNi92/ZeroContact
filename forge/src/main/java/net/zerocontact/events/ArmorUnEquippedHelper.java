package net.zerocontact.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.IEquipmentTypeTag;
import top.theillusivec4.curios.api.SlotContext;

public class ArmorUnEquippedHelper {

    public static void onArmorUnequipped(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        ItemStack armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (entity instanceof ServerPlayer player) {
            if (stack.getItem() instanceof IEquipmentTypeTag slotTag) {
                if (slotTag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.MASK)) {
                    if (headStack.getItem() instanceof IEquipmentTypeTag headType && headType.getArmorType().equals(IEquipmentTypeTag.EquipmentType.MASK)) {
                        rollBackItem(stack, player);
                    }
                } else if (slotTag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE)) {
                    if (!(armorStack.getItem() instanceof IEquipmentTypeTag equipmentTypeTag
                            && equipmentTypeTag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE_CARRIER))) {
                        rollBackItem(stack, player);
                    }
                }
            }
        }

    }

    private static void rollBackItem(ItemStack stack, Player player) {
        player.getInventory().placeItemBackInInventory(stack);
    }
}
