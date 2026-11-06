package net.zerocontact.armor.modular.module.battery.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

public class Battery extends AbstractGenerateGeoCurioItemImpl implements EquipmentModule {

    protected static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "textures/models/battery/battery.png");
    protected static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "geo/battery/battery.geo.json");
    protected static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "");
    private static final ResourceLocation trait = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "battery");

    public Battery() {
        super("battery", 12000, texture, model, animation, Type.HELMET);
        ModuleRegistry.registerCategory(ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID,"battery"), MountCategory.BATTERY);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(level.isClientSide)return;
        stack.getCapability(CapabilityRegistries.BATTERY).ifPresent(battery -> this.setDamage(stack, battery.getMaxBattery() - battery.getBattery()));

    }

    @Override
    public ResourceLocation getModuleTrait() {
        return trait;
    }

}
