package net.zerocontact.events;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ModLogger;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class PlateInteract {
    private static final Set<String> ALL_ARMOR_IDS = Set.of("jpc_armor");

    public static boolean isPlateArmorEquipped(LivingEntity entity) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!ALL_ARMOR_IDS.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().replace("zerocontact:", "")))
            return false;
        return true;
    }

    public static void onArmorUnequip(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        ItemStack ArmorItemStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (ArmorItemStack.isEmpty() && !isPlateArmorEquipped(entity) && entity instanceof Player) {
            Player player = (Player) entity;
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
