package net.zerocontact.capability;

import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttachCapListener {
    @SubscribeEvent
    public static void attachStackCap(AttachCapabilitiesEvent<ItemStack> stackAttachCapabilitiesEvent) {
        if (IGun.getIGunOrNull(stackAttachCapabilitiesEvent.getObject()) != null) {
            stackAttachCapabilitiesEvent.addCapability(new ResourceLocation(ZeroContact.MOD_ID, "cartridge"), new GunCartridgeTypeCapProvider());
            stackAttachCapabilitiesEvent.addCapability(new ResourceLocation(ZeroContact.MOD_ID,"repair_kit"), new RepairKitCapProvider());
        }
    }
}
