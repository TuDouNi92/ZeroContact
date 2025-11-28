package net.zerocontact.item.rigs;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.item.backpack.BaseBackpack;
import org.jetbrains.annotations.NotNull;

public abstract class BaseRigs extends BaseBackpack {
    public BaseRigs(ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super(texture, model, animation, containerSize);
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.RIGS;
    }
}
