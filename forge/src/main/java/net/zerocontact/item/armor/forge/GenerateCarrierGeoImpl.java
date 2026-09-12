package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.datagen.IAssetManager;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.datagen.model.GenerationRecord;
import net.zerocontact.datagen.model.ItemPOJO;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.*;

public class GenerateCarrierGeoImpl extends BaseArmorGeoImpl implements GeoItem, IEquipmentTypeTag, IAssetManager.GeneratableItem {
    protected final int defaultDurability;
    public final Set<GenerationRecord<?>> items = new HashSet<>();
    private static final EquipmentType EQUIPMENT_TYPE = EquipmentType.PLATE_CARRIER;
    private final List<ItemPOJO.Armor.Attachments> attachments;

    public GenerateCarrierGeoImpl(
            Type type, String id, int defense,
            int defaultDurability, int absorb,
            float bluntReduction, float penetrateReduction,
            float ricochetReduction, float mass, ResourceLocation texture,
            ResourceLocation model, ResourceLocation animation,
            List<ItemPOJO.Armor.Attachments> attachments
    ) {
        super(type, id, defense, defaultDurability, absorb, bluntReduction, penetrateReduction, ricochetReduction, mass, texture, model, animation);
        this.defaultDurability = defaultDurability;
        this.attachments = attachments;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ModSoundEventsReg.ARMOR_EQUIP_PLATE;
    }


    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EQUIPMENT_TYPE;
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
