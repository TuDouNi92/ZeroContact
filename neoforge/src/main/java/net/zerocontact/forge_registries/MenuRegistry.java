package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.menu.AmmoSelectorMenu;
import net.zerocontact.item.backpack.menu.BackpackContainerMenu;
import net.zerocontact.item.block.menu.WorkbenchMenu;

import static net.zerocontact.ZeroContact.MOD_ID;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MOD_ID, Registries.MENU);
    public static final RegistrySupplier<MenuType<BackpackContainerMenu>> BACKPACK_CONTAINER = MENUS.register("backpack_container", () -> IMenuTypeExtension.create(BackpackContainerMenu::new));
    public static final RegistrySupplier<MenuType<WorkbenchMenu>> WORKBENCH_MENU = MENUS.register("thoughbook_menu", () -> IMenuTypeExtension.create(WorkbenchMenu::new));
    public static final RegistrySupplier<MenuType<AmmoSelectorMenu>> AMMO_SELECTOR = MENUS.register("ammo_selector", () -> IMenuTypeExtension.create(AmmoSelectorMenu::new));
    public static final RegistrySupplier<MenuType<EquipmentMenu>> EQUIPMENT_MENU = MENUS.register("modular_equipment_menu", () -> IMenuTypeExtension.create(EquipmentMenu::new));
}
