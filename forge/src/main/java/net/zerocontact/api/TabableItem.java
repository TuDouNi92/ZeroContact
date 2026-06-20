package net.zerocontact.api;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.List;

import static net.zerocontact.forge_registries.ItemsRegForge.DEFAULT_TAB;

public interface TabableItem {
    List<RegistrySupplier<? extends ItemLike>> getItems();

    default void attach(LinkedHashMap<RegistrySupplier<? extends ItemLike>, String> tab) {
        this.getItems().forEach(item -> tab.put(item, DEFAULT_TAB));
    }
}