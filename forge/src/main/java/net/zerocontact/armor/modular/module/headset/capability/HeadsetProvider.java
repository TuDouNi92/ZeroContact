package net.zerocontact.armor.modular.module.headset.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeadsetProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final HeadsetContainer headsetContainer = new HeadsetContainer();
    private final LazyOptional<HeadsetContainer> optional =
            LazyOptional.of(() -> headsetContainer);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.HEADSET) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return headsetContainer.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        headsetContainer.deserializeNBT(arg);
    }
}
