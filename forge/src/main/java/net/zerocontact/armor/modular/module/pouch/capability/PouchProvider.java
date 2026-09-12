package net.zerocontact.armor.modular.module.pouch.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.api.armor.modular.ModuleContainer;
import net.zerocontact.armor.modular.module.pouch.container.PouchContainer;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PouchProvider implements ICapabilityProvider {
    private final PouchContainer container =
            new PouchContainer();

    private final LazyOptional<ModuleContainer> optional =
            LazyOptional.of(() -> container);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.POUCH) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}
