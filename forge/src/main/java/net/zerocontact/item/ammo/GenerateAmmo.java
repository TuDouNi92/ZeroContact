package net.zerocontact.item.ammo;

import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemAdapter;
import net.zerocontact.events.AmmoInjector;
import net.zerocontact.events.CaliberVariantDamageHelper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Optional;

import static net.zerocontact.ZeroContact.MOD_ID;

public class GenerateAmmo extends Item implements AmmoItemDataAccessor, IEquipmentTypeTag {
    private final String ammoId;
    private final String ammoVariant;
    private final float baseDamageFactor;
    private final int penetrateClass;
    private final float fleshDamage;
    private final float armorDamage;
    private final int stackSize;

    public GenerateAmmo(String ammoId, String ammoVariant, float baseDamageFactor, int penetrateClass, float fleshDamage, float armorDamage, int stackSize) {
        super(new Item.Properties().stacksTo(stackSize));
        this.ammoId = ammoId;
        this.ammoVariant = ammoVariant;
        this.baseDamageFactor = baseDamageFactor;
        this.penetrateClass = penetrateClass;
        this.fleshDamage = fleshDamage;
        this.armorDamage = armorDamage;
        this.stackSize = stackSize;
    }

    @Override
    public @NotNull ResourceLocation getAmmoId(ItemStack ammo) {
        AmmoInjector.write(new AmmoInjector.AmmoContext(
                new CaliberVariantDamageHelper.Caliber(ammoId, ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, armorDamage, stackSize)
        ), ammo);
        return new ResourceLocation(ammoId);
    }

    public CaliberVariantDamageHelper.Caliber getDefualtCaliber() {
        return new CaliberVariantDamageHelper.Caliber(ammoId, MOD_ID+":"+ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, armorDamage, stackSize);
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        LinkedHashSet<GenerationRecord<?>> ammoSet = ItemAdapter.AmmoAdapter.items;
        ItemStack[] ammoStacks = {new ItemStack(this)};
        Optional<GenerationRecord<?>> matchedRecord = ammoSet.stream().filter(record -> record.id().equals(ammoVariant)).findFirst();
        matchedRecord.ifPresent(record -> {
            if (record.item() instanceof GenerateAmmo ammo) {
                ItemStack ammoStack = new ItemStack(ammo);
                AmmoInjector.write(new AmmoInjector.AmmoContext(
                        new CaliberVariantDamageHelper.Caliber(ammoId, ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, armorDamage, stackSize)
                ), ammoStack);
                ammoStacks[0] = ammoStack;
            }
        });
        return ammoStacks[0];
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.AMMO;
    }
}
