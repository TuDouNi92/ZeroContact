package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;
import net.zerocontact.item.uniform.*;
import net.zerocontact.registries.ItemsReg;

import java.util.List;

public class Uniforms implements TabableItem {
    public static RegistrySupplier<British23Top> BRITISH23_TOP = ItemsReg.ITEMS.register("uniform_british23_top", British23Top::new);
    public static RegistrySupplier<British23Bottom> BRITISH23_BOTTOM = ItemsReg.ITEMS.register("uniform_british23_bottom", British23Bottom::new);
    public static RegistrySupplier<G99Top> G99_TOP = ItemsReg.ITEMS.register("uniform_g99_top", G99Top::new);
    public static RegistrySupplier<G99Bottom> G99_BOTTOM = ItemsReg.ITEMS.register("uniform_g99_bottom", G99Bottom::new);
    public static RegistrySupplier<SpnTop> SPN_TOP = ItemsReg.ITEMS.register("uniform_spn_top", SpnTop::new);
    public static RegistrySupplier<SpnBottom> SPN_BOTTOM = ItemsReg.ITEMS.register("uniform_spn_bottom", SpnBottom::new);
    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
            BRITISH23_TOP, BRITISH23_BOTTOM, G99_TOP, G99_BOTTOM, SPN_TOP, SPN_BOTTOM
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }
}
