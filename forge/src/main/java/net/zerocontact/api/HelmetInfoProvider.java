package net.zerocontact.api;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public interface HelmetInfoProvider {
    ArmorItem.Type getType();
    ArmorMaterial getMaterial();
    int getDefense();
    int getDefaultDurability();
    default EquipmentSlot getEquipmentSlot(){
        return EquipmentSlot.HEAD;
    }
    void initializeClient(Consumer<IClientItemExtensions> consumer);
}
