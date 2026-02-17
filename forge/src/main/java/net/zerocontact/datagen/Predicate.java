package net.zerocontact.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.api.IEquipmentTypeTag;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Predicate {
    public static void predicateCurios() {
        CuriosApi.registerCurioPredicate(new ResourceLocation(MOD_ID, "zc_predicate"), slotResult -> {
            LivingEntity entity = slotResult.slotContext().entity();
            if (entity == null) return false;
            if (slotResult.stack().getItem() instanceof PlateInfoProvider
                    && (Objects.equals(slotResult.slotContext().identifier(), "front_plate")
                    || Objects.equals(slotResult.slotContext().identifier(), "back_plate"))
            ) {
                return true;
            }
            if (slotResult.stack().getItem() instanceof IEquipmentTypeTag IEquipmentTypeTag) {
                IEquipmentTypeTag.getArmorType();
                return Objects.equals(slotResult.slotContext().identifier(), IEquipmentTypeTag.getArmorType().getTypeId().toLowerCase());
            }
            return false;
        });
    }
}
