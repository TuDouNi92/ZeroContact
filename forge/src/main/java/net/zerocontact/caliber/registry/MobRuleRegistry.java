package net.zerocontact.caliber.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.datagen.model.MobRulesPOJO;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MobRuleRegistry {
    private static final Map<ResourceLocation, MobRulesPOJO.Pattern> MOB_RULES = new HashMap<>();

    public static @Nullable MobRulesPOJO.Pattern get(ResourceLocation mobId) {
        return MOB_RULES.get(mobId);
    }

    public static void register(MobRulesPOJO pojo) {
        for (MobRulesPOJO.Pattern mob : pojo.mobs) {
            ResourceLocation mobId = new ResourceLocation(mob.mobId());
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(mobId);
            if (type != null) {
                MOB_RULES.put(mobId, mob);
            }
        }
    }
}
