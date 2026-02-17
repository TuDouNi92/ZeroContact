package net.zerocontact.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.IEquipmentTypeTag;
import top.theillusivec4.curios.api.SlotContext;

public class PlateUnEquippedHelper {

    public static void onArmorUnequipped(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        ItemStack ArmorItemStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if(entity instanceof ServerPlayer player){
            if (!(ArmorItemStack.getItem() instanceof IEquipmentTypeTag IEquipmentTypeTag)) {
                rollBackItem(stack, player);
            }
            else if(IEquipmentTypeTag.getArmorType() != net.zerocontact.api.IEquipmentTypeTag.EquipmentType.PLATE_CARRIER){
                rollBackItem(stack, player);
            }
        }

    }

    private static void rollBackItem(ItemStack stack, Player player) {
        ItemStack dropStack = stack.split(1);
        if (!dropStack.isEmpty()) {
            player.getInventory().add(dropStack);
        }
    }
}
