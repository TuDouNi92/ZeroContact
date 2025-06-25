package net.zerocontact.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.api.PlateInfoProvider;
import top.theillusivec4.curios.api.CuriosApi;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Predicate {
    public static void predicateCurios() {
        CuriosApi.registerCurioPredicate(new ResourceLocation(MOD_ID, "zc_predicate"), slotResult -> {
            LivingEntity entity = slotResult.slotContext().entity();
            if (entity == null) return false;
            return (slotResult.stack().getItem() instanceof PlateInfoProvider);
        });
    }
}
