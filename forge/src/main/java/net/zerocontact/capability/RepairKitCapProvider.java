package net.zerocontact.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.api.IRepairKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairKitCapProvider implements ICapabilityProvider {
    private final IRepairKit instance =
            new RepairKitCap();

    private final LazyOptional<IRepairKit> optional =
            LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if (capability == CapabilityRegistries.REPAIR_KIT) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}
