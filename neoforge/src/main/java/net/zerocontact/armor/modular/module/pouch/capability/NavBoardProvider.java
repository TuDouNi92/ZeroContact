package net.zerocontact.armor.modular.module.pouch.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NavBoardProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final NavBoardContainer container =
            new NavBoardContainer();

    private final LazyOptional<NavBoardContainer> optional =
            LazyOptional.of(() -> container);

    @Override
    public CompoundTag serializeNBT() {
        return container.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        container.deserializeNBT(arg);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        return CapabilityRegistries.NAV_BOARD.orEmpty(capability, optional.cast());
    }
}
