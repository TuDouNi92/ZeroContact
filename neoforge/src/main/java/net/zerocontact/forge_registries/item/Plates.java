package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;

import java.util.List;

public class Plates implements TabableItem {
    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }
}
