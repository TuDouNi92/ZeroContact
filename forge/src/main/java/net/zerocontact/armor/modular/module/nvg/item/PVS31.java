package net.zerocontact.armor.modular.module.nvg.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

public class PVS31 extends NVG {

    public PVS31() {
        super("pvs31", 12000, texture, model, animation, ArmorItem.Type.HELMET);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID,"nvg_pvs31"), MountCategory.NIGHT_VISION);
    }

}
