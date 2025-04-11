package net.zerocontact;

import com.google.common.base.Suppliers;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.registries.ItemsReg;
import net.zerocontact.registries.ModSoundEventsReg;
import net.zerocontact.item.SapiIV;

import java.util.function.Supplier;

import static net.zerocontact.item.PlateBaseMaterial.ARMOR_STEEL;

public class ZeroContact {
    public static final String MOD_ID = "zerocontact";
    // We can use this if we don't want to use DeferredRegister 没用
    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static void init() {
        ItemsReg.register();
        ModSoundEventsReg.register();
        System.out.println(ZeroContactExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
