package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.datagen.model.GenerationRecord;
import net.zerocontact.datagen.model.ItemPOJO;
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
    private final List<ItemPOJO.Armor.Attachments> attachments;

    public GenerateArmorGeoImpl(
            Type type,
            String id,
            int defense,
            int defaultDurability,
            int absorb,
            float mass,
            ResourceLocation texture,
            ResourceLocation model,
            ResourceLocation animation,
            float bluntFactor,
            float penetrateFactor,
            float ricochetFactor,
            List<ItemPOJO.Armor.Attachments> attachments) {
        super(type, id, defense, defaultDurability, absorb, bluntFactor, penetrateFactor, ricochetFactor, mass, texture, model, animation);
        this.defaultDurability = defaultDurability;
        this.bluntFactor = bluntFactor;
        this.penetrateFactor = penetrateFactor;
        this.ricochetFactor = ricochetFactor;
        this.attachments = attachments;
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

    @Override
    public Collection<MountDefinition> getMountDefinitions(ItemStack stack) {
        return attachments.stream().map(pojo ->
                new MountDefinition(
                        new ResourceLocation(ZeroContact.MOD_ID, pojo.mountId),
                        pojo.getMountType(),
                        pojo.mountBone,
                        pojo.getAcceptCategories()
                )
        ).toList();
    }
}
