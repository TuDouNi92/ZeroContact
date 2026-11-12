package net.zerocontact;

import net.zerocontact.registries.ItemsReg;

public class ZeroContact {
    public static final String MOD_ID = "zerocontact";

    public static void init() {
        ItemsReg.register();
        System.out.println(ZeroContactExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
