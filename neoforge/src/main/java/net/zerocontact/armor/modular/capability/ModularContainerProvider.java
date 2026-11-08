package net.zerocontact.armor.modular.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.api.armor.modular.ModuleContainer;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModularContainerProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final SimpleModuleContainer container =
            new SimpleModuleContainer();

    private final LazyOptional<ModuleContainer> optional =
            LazyOptional.of(() -> container);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.MODULAR_EQUIPMENT) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return container.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        container.deserializeNBT(arg);
    }
}
