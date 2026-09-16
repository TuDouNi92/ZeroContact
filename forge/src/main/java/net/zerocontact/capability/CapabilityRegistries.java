package net.zerocontact.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.zerocontact.api.armor.modular.ModuleContainer;
import net.zerocontact.api.caliber.ICartridgeHolder;
import net.zerocontact.api.IRepairKit;
import net.zerocontact.armor.modular.module.battery.container.BatteryContainer;
import net.zerocontact.armor.modular.module.beacon.container.BeaconContainer;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.armor.modular.module.nvg.container.NvgContainer;
import net.zerocontact.armor.modular.module.pouch.container.PouchContainer;

public class CapabilityRegistries {
    public static Capability<ICartridgeHolder> CARTRIDGE;
    public static Capability<IRepairKit> REPAIR_KIT;
    public static Capability<ModuleContainer> MODULAR_EQUIPMENT;
    public static Capability<PouchContainer> POUCH;
    public static Capability<NvgContainer> NVG;
    public static Capability<BatteryContainer> BATTERY;
    public static Capability<BeaconContainer> BEACON;
    public static Capability<HeadsetContainer> HEADSET;

    public static void register() {
        CARTRIDGE = CapabilityManager.get(new CapabilityToken<>() {
        });
        REPAIR_KIT = CapabilityManager.get(new CapabilityToken<>() {
        });
        MODULAR_EQUIPMENT = CapabilityManager.get(new CapabilityToken<>() {
        });
        POUCH = CapabilityManager.get(new CapabilityToken<>() {
        });
        NVG = CapabilityManager.get(new CapabilityToken<>() {
        });
        BATTERY = CapabilityManager.get(new CapabilityToken<>() {
        });
        BEACON = CapabilityManager.get(new CapabilityToken<>() {
        });
        HEADSET = CapabilityManager.get(new CapabilityToken<>() {
        });
    }

}
