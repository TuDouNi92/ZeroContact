package net.zerocontact.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.zerocontact.api.ICartridgeHolder;
import net.zerocontact.api.IRepairKit;

public class CapabilityRegistries {
    public static Capability<ICartridgeHolder> CARTRIDGE;
    public static Capability<IRepairKit> REPAIR_KIT;
    public static void register() {
        CARTRIDGE = CapabilityManager.get(new CapabilityToken<>() {});
        REPAIR_KIT = CapabilityManager.get(new CapabilityToken<>() {});
    }

}
