package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.zerocontact.entity.ArmedRaider;

import static net.zerocontact.ZeroContact.MOD_ID;
public class ModEntitiesReg {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE_DEFERRED_REGISTER = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);
    public static final RegistrySupplier<EntityType<ArmedRaider>> ARMED_RAIDER = ENTITY_TYPE_DEFERRED_REGISTER.register("armed_raider",()->EntityType.Builder.of(ArmedRaider::new,MobCategory.MONSTER).sized(.6f,2f).build(new ResourceLocation(MOD_ID,"armed_raider").toString()));
    public static void register() {
        ENTITY_TYPE_DEFERRED_REGISTER.register();
    }
}
