package net.zerocontact.fabric;

import net.zerocontact.ZeroContact;
import net.fabricmc.api.ModInitializer;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ZeroContact.init();
    }
}
