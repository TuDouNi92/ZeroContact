package net.zerocontact.armor.modular.module.nvg.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

public class T7 extends NVG {
    private static final ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, "textures/models/nvg/t7.png");
    private static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/nvg/t7.geo.json");
    private static final ResourceLocation vignette = new ResourceLocation(ZeroContact.MOD_ID, "textures/gui/bino_nvg.png");

    public T7() {
        super("t7", 12000, texture, model, animation, ArmorItem.Type.HELMET);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID, "nvg_t7"), MountCategory.NIGHT_VISION);
    }

    @Override
    public ResourceLocation getVignette() {
        return vignette;
    }

    @Override
    public INvg.Type getNVGType() {
        return INvg.Type.THERMAL;
    }
}
