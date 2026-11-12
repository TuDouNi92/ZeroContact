package net.zerocontact.armor.modular.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zerocontact.ZeroContact;

import java.util.function.Supplier;

public final class ModularDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ZeroContact.MOD_ID);

    public static final Supplier<DataComponentType<MountedModules>> MOUNTED_MODULES = COMPONENTS.register(
            "mounted_modules", () -> DataComponentType.<MountedModules>builder()
                    .persistent(MountedModules.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MountedModules.CODEC))
                    .build());

    private ModularDataComponents() {}

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
