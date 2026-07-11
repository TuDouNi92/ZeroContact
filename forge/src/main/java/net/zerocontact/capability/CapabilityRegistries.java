package net.zerocontact.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.zerocontact.api.ICartridgeHolder;

public class CapabilityRegistries {
    public static Capability<ICartridgeHolder> CARTRIDGE;
    public static void register() {
        CARTRIDGE = CapabilityManager.get(new CapabilityToken<>() {
        });
    }

}
