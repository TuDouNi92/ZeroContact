package net.zerocontact.api.armor.modular;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import java.util.function.Function;

/** Per-stack capability objects and the owning mount's component-change listener. */
public interface ModularCapabilityAccess {
    <T> T zeroContact$getModularCapability(ItemCapability<T, Void> capability, Function<ItemStack, T> factory);

    void zeroContact$setModuleChangeListener(Runnable listener);
}
