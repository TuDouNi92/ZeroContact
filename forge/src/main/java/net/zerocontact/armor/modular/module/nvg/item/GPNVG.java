package net.zerocontact.armor.modular.module.nvg.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

public class GPNVG extends NVG {
    protected static final ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, "textures/models/nvg/gpnvg.png");
    protected static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/nvg/gpnvg.geo.json");
    protected static final ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, "animations/nvg_pvs31.animation.json");
    private static final ResourceLocation vignette = new ResourceLocation(ZeroContact.MOD_ID, "textures/gui/gpnvg.png");

    public GPNVG() {
        super("gpnvg", 12000, texture, model, animation, ArmorItem.Type.HELMET);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID, "nvg_gpnvg"), MountCategory.NIGHT_VISION);
    }

    @Override
    public  ResourceLocation getVignette() {
        return vignette;
    }

    @Override
    public INvg.Type getNVGType() {
        return INvg.Type.WHITE;
    }
}
