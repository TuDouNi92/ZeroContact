package net.zerocontact.armor.modular.module.beacon.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.armor.modular.module.beacon.container.BeaconContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeaconProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final BeaconContainer beaconContainer = new BeaconContainer();
    private final LazyOptional<BeaconContainer> optional =
            LazyOptional.of(() -> beaconContainer);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.BEACON) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return beaconContainer.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        beaconContainer.deserializeNBT(arg);
    }
}
