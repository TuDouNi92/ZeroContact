package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;

import java.util.List;

public class Plates implements TabableItem {
//    public static RegistrySupplier<BasePlate> CULT_LOCUST_PLATE = ItemsReg.ITEMS.register("plate_cult_locust",
//            () -> BasePlate.createGeoPlate(128, 0, 8, 0.1f, 0.75f, -0.025f, "textures/models/plate/plate_cult_locust.png", "geo/plate/plate_cult_locust.geo.json", ""));
//    public static RegistrySupplier<BasePlate> BALLISTIC_CONVOY = ItemsReg.ITEMS.register("plate_ballistic_convoy",
//            () -> BasePlate.createGeoPlate(168, 0, 11, 0.05f, 0.87f, -0.05f, "textures/models/plate/plate_cult_locust.png", "geo/plate/plate_cult_locust.geo.json", ""));
//    public static RegistrySupplier<BasePlate> STEEL_PLATE = ItemsReg.ITEMS.register("plate_steel",
//            () -> BasePlate.createGeoPlate(72, 0, 6, 0.25f, 0.9f, -0.010f, "textures/models/plate/plate_cult_locust.png", "geo/plate/plate_cult_locust.geo.json", ""));
//    public static RegistrySupplier<BasePlate> SLIME_PLATE = ItemsReg.ITEMS.register("plate_slime",
//            () -> BasePlate.createGeoPlate(32, 0, 4, 0.2f, 1f, 0f, "textures/models/plate/plate_slime.png", "geo/plate/plate_slime.geo.json", ""));
    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
//            CULT_LOCUST_PLATE, BALLISTIC_CONVOY, STEEL_PLATE, SLIME_PLATE
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }
}
