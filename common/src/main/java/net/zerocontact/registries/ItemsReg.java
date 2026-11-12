package net.zerocontact.registries;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ItemsReg {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);

    public static void register() {
        ITEMS.register();
    }
}
