package net.zerocontact.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.api.armor.PlateInfoProvider;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Predicate {
    public static void predicateCurios() {
        CuriosApi.registerCurioPredicate(new ResourceLocation(MOD_ID, "zc_predicate"), slotResult -> {
            LivingEntity entity = slotResult.slotContext().entity();
            if (entity == null) return false;
            if (slotResult.stack().getItem() instanceof PlateInfoProvider
                    && (Objects.equals(slotResult.slotContext().identifier(), CuriosConstants.FRONT_PLATE)
                    || Objects.equals(slotResult.slotContext().identifier(), CuriosConstants.BACK_PLATE))
            ) {
                return true;
            }
            if (slotResult.stack().getItem() instanceof IEquipmentTypeTag equipmentTypeTag) {
                String slotId = switch (equipmentTypeTag.getArmorType()) {
                    case MASK -> CuriosConstants.MASK;
                    case UNIFORM_TOP -> CuriosConstants.UNIFORM_TOP;
                    case UNIFORM_PANTS -> CuriosConstants.UNIFORM_PANTS;
                    case ARMBAND -> CuriosConstants.ARMBAND;
                    case BACKPACK -> CuriosConstants.BACKPACK;
                    case RIGS -> CuriosConstants.RIGS;
                    default -> equipmentTypeTag.getArmorType().getTypeId().toLowerCase();
                };
                return Objects.equals(slotResult.slotContext().identifier(), slotId);
            }
            return false;
        });
    }
}
