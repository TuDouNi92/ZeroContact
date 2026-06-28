package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.*;

public class GenerateArmorGeoImpl extends BaseArmorGeoImpl implements GeoItem, IEquipmentTypeTag {
    protected final int defaultDurability;
    public final Set<GenerationRecord<?>> items = new HashSet<>();
    private final float bluntFactor;
    private final float penetrateFactor;
    private final float ricochetFactor;

    public GenerateArmorGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, float bluntFactor, float penetrateFactor, float ricochetFactor) {
        super(type, id, defense, defaultDurability, absorb, bluntFactor, penetrateFactor, ricochetFactor, mass, texture, model, animation);
        this.defaultDurability = defaultDurability;
        this.bluntFactor = bluntFactor;
        this.penetrateFactor = penetrateFactor;
        this.ricochetFactor = ricochetFactor;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ModSoundEventsReg.ARMOR_EQUIP_PLATE;
    }

    @Override
    public float generateBlunt() {
        return this.bluntFactor;
    }

    @Override
    public float generateRicochet() {
        return this.ricochetFactor;
    }

    @Override
    public float generatePenetrated() {
        return this.penetrateFactor;
    }


}
