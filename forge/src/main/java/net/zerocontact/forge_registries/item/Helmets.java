package net.zerocontact.forge_registries.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.api.TabableItem;
import net.zerocontact.item.helmet.*;
import net.zerocontact.registries.ItemsReg;

import java.util.List;

public class Helmets implements TabableItem {
    public static RegistrySupplier<FastMt> FAST_MT = ItemsReg.ITEMS.register("fast_mt", () -> new FastMt(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties(), 8));
    public static RegistrySupplier<Ratnik> RATNIK_HELMET_EMR = ItemsReg.ITEMS.register("helmet_6b47_ratnik_emr", () -> Ratnik.create(4, 32, Ratnik.Color.EMR));
    public static RegistrySupplier<Ratnik> RATNIK_HELMET_ARC = ItemsReg.ITEMS.register("helmet_6b47_ratnik_arc", () -> Ratnik.create(4, 32, Ratnik.Color.ARCTIC));
    public static RegistrySupplier<Bastion> BASTION_HELMET = ItemsReg.ITEMS.register("helmet_bastion_black", () -> Bastion.create(9, 48, Bastion.Color.BLACK));
    public static RegistrySupplier<Bastion> BASTION_HELMET_MULTICAM = ItemsReg.ITEMS.register("helmet_bastion_multicam", () -> Bastion.create(9, 48, Bastion.Color.MULTICAM));
    public static RegistrySupplier<Bastion> BASTION_HELMET_GREEN = ItemsReg.ITEMS.register("helmet_bastion_green", () -> Bastion.create(9, 48, Bastion.Color.GREEN));
    public static RegistrySupplier<Untar> UNTAR_HELMET = ItemsReg.ITEMS.register("helmet_untar_blue", () -> new Untar(6, 24));
    public static RegistrySupplier<AltynVisor.WithVisor> ALTYN_VISOR_HELMET = ItemsReg.ITEMS.register("helmet_altyn_visor", () -> new AltynVisor.WithVisor(10, 72));
    public static RegistrySupplier<AirFrame> AIRFRAME_HELMET = ItemsReg.ITEMS.register("helmet_airframe", () -> new AirFrame(8, 42));
    public static RegistrySupplier<net.zerocontact.item.helmet.British23> BRITISH23_HELMET = ItemsReg.ITEMS.register("helmet_british23", () -> new net.zerocontact.item.helmet.British23(0, 128));
    public static RegistrySupplier<PhoneTalker> PHONETALKER_HELMET = ItemsReg.ITEMS.register("helmet_phonetalker_iiia", () -> new PhoneTalker(6, 32));
    public static RegistrySupplier<TBH> TBH_HELMET = ItemsReg.ITEMS.register("helmet_tbh_iiia", () -> new TBH(7, 36));
    public static RegistrySupplier<TagillaMask> TAGILLA_MASK_MANHUNT = ItemsReg.ITEMS.register("mask_tagilla_manhunt", () -> TagillaMask.create(12, 64, TagillaMask.Color.MANHUNT));
    public static RegistrySupplier<TagillaMask> TAGILLA_MASK_YBEY = ItemsReg.ITEMS.register("mask_tagilla_ybey", () -> TagillaMask.create(12, 64, TagillaMask.Color.YBEY));
    public static RegistrySupplier<GasMask> PMK2 = ItemsReg.ITEMS.register("mask_pmk2", () -> GasMask.create(GasMask.Series.PMK2, 2, 12));
    public static RegistrySupplier<GasMask> ZK = ItemsReg.ITEMS.register("mask_zk", () -> GasMask.create(GasMask.Series.ZK, 2, 12));
    public static RegistrySupplier<GasMask> M50 = ItemsReg.ITEMS.register("mask_m50", () -> GasMask.create(GasMask.Series.M50, 2, 12));
    public static RegistrySupplier<GasMask> MP5 = ItemsReg.ITEMS.register("mask_mp5", () -> GasMask.create(GasMask.Series.MP5, 2, 12));
    public static RegistrySupplier<ColdFearMask> COLD_FEAR_MASK = ItemsReg.ITEMS.register("mask_cold_fear", () -> new ColdFearMask(8, 24));
    public static RegistrySupplier<Cap> CYAN_CAP = ItemsReg.ITEMS.register("cap_cyan", () -> Cap.create(0, 24, Cap.Color.CYAN));
    public static RegistrySupplier<Cap> BOSS_CAP = ItemsReg.ITEMS.register("cap_boss", () -> Cap.create(0, 24, Cap.Color.BOSS));


    private static final List<RegistrySupplier<? extends ItemLike>> items = List.of(
            FAST_MT, RATNIK_HELMET_EMR, RATNIK_HELMET_ARC, BASTION_HELMET, BASTION_HELMET_MULTICAM,
            BASTION_HELMET_GREEN, UNTAR_HELMET, ALTYN_VISOR_HELMET, AIRFRAME_HELMET, BRITISH23_HELMET,
            PHONETALKER_HELMET, TBH_HELMET, TAGILLA_MASK_MANHUNT, TAGILLA_MASK_YBEY, PMK2, ZK, M50, MP5,
            COLD_FEAR_MASK, CYAN_CAP, BOSS_CAP
    );

    @Override
    public List<RegistrySupplier<? extends ItemLike>> getItems() {
        return items;
    }
}
