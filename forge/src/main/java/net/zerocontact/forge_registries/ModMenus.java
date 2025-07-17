package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.zerocontact.client.menu.BackpackContainerMenu;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MOD_ID, Registries.MENU);
    public static final RegistrySupplier<MenuType<BackpackContainerMenu>> BACKPACK_CONTAINER = MENUS.register("backpack_container",()->IForgeMenuType.create(BackpackContainerMenu::new));
}
