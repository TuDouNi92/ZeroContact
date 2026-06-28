package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.*;

public class GenerateCarrierGeoImpl extends BaseArmorGeoImpl implements GeoItem, IEquipmentTypeTag, IAssetManager.GeneratableItem {
    protected final int defaultDurability;
    public final Set<GenerationRecord<?>> items = new HashSet<>();
    private static final EquipmentType EQUIPMENT_TYPE = EquipmentType.PLATE_CARRIER;

    public GenerateCarrierGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float bluntReduction, float penetrateReduction, float ricochetReduction, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(type, id, defense, defaultDurability, absorb, bluntReduction, penetrateReduction, ricochetReduction, mass, texture, model, animation);
        this.defaultDurability = defaultDurability;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ModSoundEventsReg.ARMOR_EQUIP_PLATE;
    }


    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EQUIPMENT_TYPE;
    }
}
