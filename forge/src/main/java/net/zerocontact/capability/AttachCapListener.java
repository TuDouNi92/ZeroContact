package net.zerocontact.capability;

import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.armor.modular.capability.ModularContainerProvider;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttachCapListener {
    @SubscribeEvent
    public static void attachStackCap(AttachCapabilitiesEvent<ItemStack> event) {
        if (IGun.getIGunOrNull(event.getObject()) != null) {
            event.addCapability(
                    new ResourceLocation(ZeroContact.MOD_ID, "cartridge"), new GunCartridgeTypeCapProvider()
            );
        }

        if (event.getObject().getItem() instanceof ModularEquipment) {
            event.addCapability(
                    new ResourceLocation(ZeroContact.MOD_ID, "modular_equipment"),
                    new ModularContainerProvider()
            );
        }

        if (event.getObject().getItem() instanceof EquipmentModule module) {
            ModuleRegistry.CapabilityEntry trait = ModuleRegistry.getTrait(module.getModuleTrait());
            event.addCapability(
                    module.getModuleTrait(),
                    trait.providerFactory().get()
            );
        }
    }
}
