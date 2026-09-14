package net.zerocontact.armor.modular.module.beacon.item;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;

public class Beacon extends AbstractGenerateGeoCurioItemImpl implements EquipmentModule {
    private static final ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, "textures/models/beacon/beacon.png");
    private static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/beacon/beacon.geo.json");
    private static final ResourceLocation animation = new ResourceLocation("");
    private static final ResourceLocation trait = new ResourceLocation(ZeroContact.MOD_ID, "beacon");

    public Beacon() {
        super("beacon", 0, texture, model, animation, Type.HELMET);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID,"beacon"), MountCategory.BEACON);
    }

    @Override
    public ResourceLocation getModuleTrait() {
        return trait;
    }
}
