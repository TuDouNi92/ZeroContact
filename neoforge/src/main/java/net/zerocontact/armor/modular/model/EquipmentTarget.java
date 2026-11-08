package net.zerocontact.armor.modular.model;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.LinkedHashMap;
import java.util.Map;

/** A worn slot address, resolved against the receiving player's own equipment. */
public record EquipmentTarget(String slot, int index) {
    public static final EquipmentTarget NONE = new EquipmentTarget("", 0);

    public ItemStack resolve(Player player) {
        if (index < 0) return ItemStack.EMPTY;
        for (EquipmentSlot armorSlot : EquipmentSlot.values()) {
            if (armorSlot.getType() == EquipmentSlot.Type.ARMOR && index == 0
                    && slot.equals("armor/" + armorSlot.getName())) return player.getItemBySlot(armorSlot);
        }
        if (!slot.startsWith("curios/")) return ItemStack.EMPTY;
        return CuriosApi.getCuriosInventory(player).map(inventory ->
                inventory.getStacksHandler(slot.substring(7)).filter(handler -> index < handler.getStacks().getSlots()).map(handler -> handler.getStacks().getStackInSlot(index)).orElse(ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
    }

    public static Map<EquipmentTarget, ItemStack> wornBy(Player player) {
        Map<EquipmentTarget, ItemStack> result = new LinkedHashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                result.put(new EquipmentTarget("armor/" + slot.getName(), 0), player.getItemBySlot(slot));
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().forEach((id, handler) -> {
                    for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                        result.put(new EquipmentTarget("curios/" + id, i), handler.getStacks().getStackInSlot(i));
                    }
                }));
        return result;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(slot, 128);
        buf.writeVarInt(index);
    }

    public static EquipmentTarget read(FriendlyByteBuf buf) {
        return new EquipmentTarget(buf.readUtf(128), buf.readVarInt());
    }
}
