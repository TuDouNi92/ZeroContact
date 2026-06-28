package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;
import net.zerocontact.item.armor.forge.*;
import net.zerocontact.registries.ItemsReg;

import java.util.List;

public class Armors implements TabableItem {
//    public static RegistrySupplier<ThorArmorImpl> THOR_ARMOR = ItemsReg.ITEMS.register("armor_thor_black", () -> new ThorArmorImpl(0, 35, 0, 0.35f, 0.95f, -0.01F));
//    public static RegistrySupplier<UntarArmorImpl> UNTAR_ARMOR = ItemsReg.ITEMS.register("armor_untar_blue", () -> new UntarArmorImpl(6, 24, 6, 0.20f, 0.98f, -0.03F));
//    public static RegistrySupplier<HexgridArmorImpl> HEXGRID_ARMOR = ItemsReg.ITEMS.register("armor_hexgrid_black", () -> new HexgridArmorImpl(2, 31, 2, 0.5f, 1, 0.01F));
//    public static RegistrySupplier<R6b2ArmorImpl> R6B2 = ItemsReg.ITEMS.register("armor_6b2", () -> new R6b2ArmorImpl(4, 12, 5, 0.4f, 1.05f, -0.05F));
//    public static RegistrySupplier<R6b23IArmorImpl> R6B23I = ItemsReg.ITEMS.register("armor_6b23_1", () -> new R6b23IArmorImpl(7, 48, 7, 0.2f, 0.8f, -0.025F));
//    public static RegistrySupplier<R6b23IIArmorImpl> R6B23II = ItemsReg.ITEMS.register("armor_6b23_2", () -> new R6b23IIArmorImpl(7, 48, 7, 0.2f, 0.8f, -0.025F));
//    public static RegistrySupplier<Defender2ArmorImpl> DEFENDER = ItemsReg.ITEMS.register("armor_defender_2", () -> new Defender2ArmorImpl(4, 64, 4, 0.08f, 0.85f, -0.015F));
//    public static RegistrySupplier<R6b43ArmorImpl> R6B43 = ItemsReg.ITEMS.register("armor_6b43", () -> R6b43ArmorImpl.create(R6b43ArmorImpl.Series.FLORA, 12, 64, 12, 0.05f, .7f, -0.1F));
//    public static RegistrySupplier<JpcArmorImpl> JPC_V1 = ItemsReg.ITEMS.register("armor_jpc_v1", () -> JpcArmorImpl.create(JpcArmorImpl.Series.V1, 0, 32, 0, 0.25f, 0.95f, 0.01F));
//    public static RegistrySupplier<JpcArmorImpl> JPC_V2 = ItemsReg.ITEMS.register("armor_jpc_v2", () -> JpcArmorImpl.create(JpcArmorImpl.Series.V2, 0, 32, 0, 0.25f, 0.95f, 0.01F));
//    public static RegistrySupplier<JpcArmorImpl> JPC_V2_SC = ItemsReg.ITEMS.register("armor_jpc_v2_swimmer_cut", () -> JpcArmorImpl.create(JpcArmorImpl.Series.V2SC, 0, 16, 0, 0.25f, 0.95f, 0.01F));
//    public static RegistrySupplier<AvsArmorImpl> AVS = ItemsReg.ITEMS.register("armor_avs", () -> new AvsArmorImpl(0, 48, 0, 0.20f, 0.85f, -0.03F));
    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
//            THOR_ARMOR, UNTAR_ARMOR, HEXGRID_ARMOR, R6B2,
//            R6B23I, R6B23II, DEFENDER, R6B43, JPC_V1, JPC_V2,
//            JPC_V2_SC, AVS
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }

}
