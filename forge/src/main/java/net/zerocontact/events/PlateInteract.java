package net.zerocontact.events;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ArmorTypeTag;
import top.theillusivec4.curios.api.SlotContext;

public class PlateInteract {
    public static boolean isPlateArmorEquipped(LivingEntity entity) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        return stack.getItem() instanceof ArmorTypeTag;
    }

    public static void onArmorUnequip(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        ItemStack ArmorItemStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (ArmorItemStack.isEmpty() && !isPlateArmorEquipped(entity) && entity instanceof Player player) {
            ItemStack dropStack = stack.split(1);
            rollBackItem(dropStack, player);
        }
    }

    private static void rollBackItem(ItemStack stack, Player player) {
        if (!stack.isEmpty()) {
            player.getInventory().add(stack);
        }
    }
}
