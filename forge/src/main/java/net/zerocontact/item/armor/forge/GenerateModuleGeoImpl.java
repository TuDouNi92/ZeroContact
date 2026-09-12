package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;

public class GenerateModuleGeoImpl extends AbstractGenerateGeoCurioItemImpl implements EquipmentModule {
    private final ResourceLocation moduleTrait;

    public GenerateModuleGeoImpl(int defaultDurability, ResourceLocation moduleTrait, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super("", defaultDurability, texture, model, animation, Type.CHESTPLATE);
        this.moduleTrait = moduleTrait;
    }

    @Override
    public ResourceLocation getModuleTrait() {
        return moduleTrait;
    }
}
