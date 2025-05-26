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
import net.zerocontact.item.Ceramic;
import net.zerocontact.item.SapiIV;
import net.zerocontact.item.armor.AvsArmor;
import net.zerocontact.item.armor.JpcArmor;

import static net.zerocontact.ZeroContact.MOD_ID;
import static net.zerocontact.item.PlateBaseMaterial.ARMOR_STEEL;
import static net.zerocontact.item.PlateBaseMaterial.SLIME_STEEL;

public class ItemsReg {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> ZERO_CONTACT = TABS.register("zero_contact", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + ".zero_contact"),
                    () -> new ItemStack(ItemsReg.STEEL_PLATE.get())));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> STEEL_PLATE = ITEMS.register("steel_plate", () ->
            SapiIV.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties().arch$tab(ZERO_CONTACT),10,7,-0.04f));

    public static void register() {
        TABS.register();
        ITEMS.register("jpc_armor", () ->
                JpcArmor.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties().arch$tab(ZERO_CONTACT)));
        ITEMS.register("si_plate",
                () -> SapiIV.create(SLIME_STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties().arch$tab(ZERO_CONTACT),5,6,-0.01f));
        ITEMS.register("bc_plate",
                () -> SapiIV.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties().arch$tab(ZERO_CONTACT),10,10,-0.05f));
        ITEMS.register("ceramic_plate",
                ()-> Ceramic.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE,new Item.Properties().arch$tab(ZERO_CONTACT),10,8,-0.03f));
        ITEMS.register("avs_armor", () ->
                AvsArmor.create(ARMOR_STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties().arch$tab(ZERO_CONTACT)));
        ITEMS.register("steel_ingot",()->new Item(new Item.Properties().arch$tab(ZERO_CONTACT)));
        ITEMS.register("steel_sheet",()->new Item(new Item.Properties().arch$tab(ZERO_CONTACT)));
        ITEMS.register("ceramic_shatters",()->new Item(new Item.Properties().arch$tab(ZERO_CONTACT)));
        ITEMS.register("fabric_roll",()->new Item(new Item.Properties().arch$tab(ZERO_CONTACT)));
        ITEMS.register();
    }
}
