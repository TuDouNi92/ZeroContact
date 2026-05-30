package net.zerocontact.item.ammo;

import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import net.minecraft.nbt.CompoundTag;
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

public class GenerateAmmo extends Item implements AmmoItemDataAccessor, IEquipmentTypeTag {
    private final String ammoId;
    private final String ammoVariant;
    private final float baseDamageFactor;
    private final int penetrateClass;
    private final float fleshDamage;
    private final int stackSize;

    public GenerateAmmo(String ammoId, String ammoVariant, float baseDamageFactor, int penetrateClass, float fleshDamage, int stackSize) {
        super(new Item.Properties().stacksTo(stackSize));
        this.ammoId = ammoId;
        this.ammoVariant = ammoVariant;
        this.baseDamageFactor = baseDamageFactor;
        this.penetrateClass = penetrateClass;
        this.fleshDamage = fleshDamage;
        this.stackSize = stackSize;
    }

    @Override
    public @NotNull ResourceLocation getAmmoId(ItemStack ammo) {
        AmmoInjector.write(new AmmoInjector.AmmoContext(
                new CaliberVariantDamageHelper.Caliber(ammoId, ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, stackSize)
        ), ammo);
        return new ResourceLocation(ammoId);
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
                        new CaliberVariantDamageHelper.Caliber(ammoId, ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, stackSize)
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
