package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;
import net.zerocontact.item.armband.Armband;
import net.zerocontact.registries.ItemsReg;

import java.util.List;

public class Armbands implements TabableItem {
    public static RegistrySupplier<Armband> ARMBAND_BLACK = ItemsReg.ITEMS.register("armband_black", () -> Armband.create(Armband.Series.BLACK));
    public static RegistrySupplier<Armband> ARMBAND_RED = ItemsReg.ITEMS.register("armband_red", () -> Armband.create(Armband.Series.RED));
    public static RegistrySupplier<Armband> ARMBAND_GREEN = ItemsReg.ITEMS.register("armband_green", () -> Armband.create(Armband.Series.GREEN));
    public static RegistrySupplier<Armband> ARMBAND_BLUE = ItemsReg.ITEMS.register("armband_blue", () -> Armband.create(Armband.Series.BLUE));
    public static RegistrySupplier<Armband> ARMBAND_WHITE = ItemsReg.ITEMS.register("armband_white", () -> Armband.create(Armband.Series.WHITE));
    public static RegistrySupplier<Armband> ARMBAND_YELLOW = ItemsReg.ITEMS.register("armband_yellow", () -> Armband.create(Armband.Series.YELLOW));
    public static RegistrySupplier<Armband> ARMBAND_FLORA = ItemsReg.ITEMS.register("armband_flora", () -> Armband.create(Armband.Series.FLORA));
    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
            ARMBAND_BLACK,
            ARMBAND_RED,
            ARMBAND_GREEN,
            ARMBAND_BLUE,
            ARMBAND_WHITE,
            ARMBAND_YELLOW,
            ARMBAND_FLORA
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }
}
