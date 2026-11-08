package net.zerocontact.armor.modular.module.nvg.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.armor.modular.module.nvg.container.NvgContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NvgProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final NvgContainer nvgContainer = new NvgContainer();
    private final LazyOptional<NvgContainer> optional =
            LazyOptional.of(() -> nvgContainer);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.NVG) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return nvgContainer.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        nvgContainer.deserializeNBT(arg);
    }
}
