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

import java.util.function.Supplier;

import static net.zerocontact.PlateBaseMaterial.ARMOR_STEEL;

public class ZeroContact {
    public static final String MOD_ID = "zerocontact";
    // We can use this if we don't want to use DeferredRegister 没用
    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    // Registering a new creative tab 注册新TAB
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> EXAMPLE_TAB = TABS.register("zero_contact", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + ".zero_contact"),
                    () -> new ItemStack(ZeroContact.EXAMPLE_ITEM.get())));

    //注册新物品，先用defer创建好再注册
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> EXAMPLE_ITEM = ITEMS.register("steel_plate", () ->
             SapiIV.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE,new Item.Properties().arch$tab(EXAMPLE_TAB)));


    public static void init() {
        TABS.register();
        ITEMS.register();
        ModSoundEvents.register();
        System.out.println(ZeroContactExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
