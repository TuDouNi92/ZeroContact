package net.zerocontact.armor.modular.module.nvg.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

public class PVS31A extends NVG {
    private static final ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, "textures/models/nvg/nvg_pvs31a.png");
    private static final ResourceLocation vignette = new ResourceLocation(ZeroContact.MOD_ID, "textures/gui/bino_nvg.png");

    public PVS31A() {
        super("pvs31a", 12000, texture, model, animation, ArmorItem.Type.HELMET);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID, "nvg_pvs31a"), MountCategory.NIGHT_VISION);
    }

    @Override
    public INvg.Type getNVGType() {
        return INvg.Type.WHITE;
    }

    @Override
    public ResourceLocation getVignette() {
        return vignette;
    }
}
