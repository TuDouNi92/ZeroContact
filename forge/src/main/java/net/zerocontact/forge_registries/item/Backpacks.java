package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;
import net.zerocontact.item.backpack.British23;
import net.zerocontact.item.backpack.T20;
import net.zerocontact.item.backpack.Vkbo;
import net.zerocontact.item.rigs.Rigs;
import net.zerocontact.registries.ItemsReg;

import java.util.List;

public class Backpacks implements TabableItem {
    public static RegistrySupplier<T20> T20_BACKPACK_UMBRA = ItemsReg.ITEMS.register("backpack_t20_umbra", () -> T20.create(T20.Series.UMBRA, 25));
    public static RegistrySupplier<T20> T20_BACKPACK_MULTICAM = ItemsReg.ITEMS.register("backpack_t20_multicam", () -> T20.create(T20.Series.MULTICAM, 25));
    public static RegistrySupplier<British23> BRITISH23_BACKPACK_RED = ItemsReg.ITEMS.register("backpack_british23_red", () -> new British23(23));
    public static RegistrySupplier<Vkbo> VKBO_BACKPACK_OLIVE = ItemsReg.ITEMS.register("backpack_vkbo_olive", () -> new Vkbo(8));
    public static RegistrySupplier<Rigs> THUNDERBOLT_RIGS_GREY = ItemsReg.ITEMS.register("rigs_thunderbolt_gray", () -> Rigs.create(Rigs.Series.THUNDERBOLT, 16));
    public static RegistrySupplier<Rigs> ALICE_RIGS_OLIVE = ItemsReg.ITEMS.register("rigs_alice_olive", () -> Rigs.create(Rigs.Series.ALICE, 6));
    public static RegistrySupplier<Rigs> CR498_RIGS_DESERT = ItemsReg.ITEMS.register("rigs_cr498_desert", () -> Rigs.create(Rigs.Series.CR498, 22));
    public static RegistrySupplier<Rigs> SOP_MR_RIGS = ItemsReg.ITEMS.register("rigs_sop_mr_desert", () -> Rigs.create(Rigs.Series.SOP_MR, 8));
    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
            T20_BACKPACK_UMBRA,
            T20_BACKPACK_MULTICAM,
            BRITISH23_BACKPACK_RED,
            VKBO_BACKPACK_OLIVE,
            THUNDERBOLT_RIGS_GREY,
            ALICE_RIGS_OLIVE,
            CR498_RIGS_DESERT,
            SOP_MR_RIGS
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }
}
