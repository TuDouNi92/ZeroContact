package net.zerocontact;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.RegistrarManager;
import net.zerocontact.registries.ItemsReg;
import net.zerocontact.registries.ModSoundEventsReg;

import java.util.function.Supplier;

public class ZeroContact {
    public static final String MOD_ID = "zerocontact";
    // We can use this if we don't want to use DeferredRegister 没用
    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static void init() {
        ItemsReg.register();
        System.out.println(ZeroContactExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
