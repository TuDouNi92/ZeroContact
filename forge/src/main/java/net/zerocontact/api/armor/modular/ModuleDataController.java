package net.zerocontact.api.armor.modular;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.armor.modular.model.ActionResult;
import net.zerocontact.armor.modular.model.ModuleContext;

public interface ModuleDataController extends ModuleController{
    ActionResult executeData(
            ModuleContext context,
            ResourceLocation operationId,
            CompoundTag payload
    );
}
