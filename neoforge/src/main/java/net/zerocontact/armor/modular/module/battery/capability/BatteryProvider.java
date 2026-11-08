package net.zerocontact.armor.modular.module.battery.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.armor.modular.module.battery.container.BatteryContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BatteryProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final BatteryContainer batteryContainer = new BatteryContainer();
    private final LazyOptional<BatteryContainer> optional =
            LazyOptional.of(() -> batteryContainer);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.BATTERY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return batteryContainer.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        batteryContainer.deserializeNBT(arg);
    }
}
