package net.zerocontact.armor.modular.module.nvg.api;

import net.minecraft.resources.ResourceLocation;

public interface INvg {
    ResourceLocation getVignette();

    ResourceLocation getAnimation();

    default Type getNVGType() {
        return Type.GREEN;
    }


    enum Type {
        GREEN,
        WHITE,
        THERMAL,
        THERMAL_COLOR
    }
}
