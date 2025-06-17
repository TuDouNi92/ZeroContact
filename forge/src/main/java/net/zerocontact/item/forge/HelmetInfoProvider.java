package net.zerocontact.item.forge;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;

interface HelmetInfoProvider {
    ArmorItem.Type getType();
    ArmorMaterial getMaterial();
    int getDefense();
    int getDefaultDurability();
    default EquipmentSlot getEquipmentSlot(){
        return EquipmentSlot.HEAD;
    }
    void initializeClient(Consumer<IClientItemExtensions> consumer);
}
