package net.zerocontact.mixin.minecraft;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.zerocontact.api.armor.modular.ModularCapabilityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(ItemStack.class)
public abstract class ModularItemStackMixin implements ModularCapabilityAccess {
    @Unique
    private Map<ItemCapability<?, ?>, Object> zeroContact$moduleCapabilities;
    @Unique
    private Runnable zeroContact$moduleChangeListener;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T zeroContact$getModularCapability(ItemCapability<T, Void> capability, Function<ItemStack, T> factory) {
        if (zeroContact$moduleCapabilities == null) zeroContact$moduleCapabilities = new IdentityHashMap<>();
        return (T) zeroContact$moduleCapabilities.computeIfAbsent(capability,
                ignored -> factory.apply((ItemStack) (Object) this));
    }

    @Override
    public void zeroContact$setModuleChangeListener(Runnable listener) {
        zeroContact$moduleChangeListener = listener;
    }

    // This also captures inventory, damage and GeckoLib animation-ID changes on mounted stacks.
    // Copies have no listener: immutable component snapshots cannot write back to a live mount.
    @Inject(method = {
            "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
            "remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
    }, at = @At("RETURN"))
    private void zeroContact$moduleComponentChanged(CallbackInfoReturnable<Object> cir) {
        if (zeroContact$moduleChangeListener != null) zeroContact$moduleChangeListener.run();
    }

    @Inject(method = {
            "applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V",
            "applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V",
            "applyComponentsAndValidate(Lnet/minecraft/core/component/DataComponentPatch;)V"
    }, at = @At("RETURN"))
    private void zeroContact$moduleComponentsApplied(CallbackInfo ci) {
        if (zeroContact$moduleChangeListener != null) zeroContact$moduleChangeListener.run();
    }
}
