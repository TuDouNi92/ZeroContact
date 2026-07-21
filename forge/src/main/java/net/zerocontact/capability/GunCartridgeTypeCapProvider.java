package net.zerocontact.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.api.ICartridgeHolder;
import org.jetbrains.annotations.NotNull;

public class GunCartridgeTypeCapProvider implements ICapabilityProvider {
    private final ICartridgeHolder instance =
            new GunCartridgeTypeCap();

    private final LazyOptional<ICartridgeHolder> optional =
            LazyOptional.of(() -> instance);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> cap,
            Direction side
    ) {

        if (cap == CapabilityRegistries.CARTRIDGE) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    public void invalidate() {
        optional.invalidate();
    }
}
