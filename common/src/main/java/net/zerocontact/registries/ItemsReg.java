package net.zerocontact.registries;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.item.SapiIV;
import net.zerocontact.item.armor.JpcArmor;

import static net.zerocontact.ZeroContact.MOD_ID;
import static net.zerocontact.item.PlateBaseMaterial.ARMOR_STEEL;

public class ItemsReg {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> ZERO_CONTACT = TABS.register("zero_contact", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + ".zero_contact"),
                    () -> new ItemStack(ItemsReg.STEEL_PLATE.get())));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> STEEL_PLATE = ITEMS.register("steel_plate", () ->
            SapiIV.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE,new Item.Properties().arch$tab(ZERO_CONTACT)));
    public static final RegistrySupplier<Item> JPC_ARMOR = ITEMS.register("jpc_armor",()->
            JpcArmor.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE,new Item.Properties().arch$tab(ZERO_CONTACT)));
    public static void register(){
        TABS.register();
        ITEMS.register();
    }

}
