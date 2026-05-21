package net.zerocontact.item.armor.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.models.GenerateModel;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.*;
import java.util.function.Consumer;

public class GenerateCarrierGeoImpl extends BaseArmorGeoImpl implements GeoItem, IEquipmentTypeTag, IAssetManager.GeneratableItem {
    protected final int defaultDurability;
    public final Set<GenerationRecord<?>> items = new HashSet<>();
    private static final EquipmentType EQUIPMENT_TYPE = EquipmentType.PLATE_CARRIER;

    public GenerateCarrierGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float bluntReduction, float penetrateReduction, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(type, id, defense, defaultDurability, absorb, bluntReduction, penetrateReduction, mass, texture, model, animation);
        this.defaultDurability = defaultDurability;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ModSoundEventsReg.ARMOR_EQUIP_PLATE;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender<GenerateArmorGeoImpl> render;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (texture == null || model == null) return this.render;
                if (render == null) {
                    render = new ArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                render.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return render;
            }
        });
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.CHEST) return super.getAttributeModifiers(slot, stack);
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        modifierMultimap.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor").getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
        return modifierMultimap;
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EQUIPMENT_TYPE;
    }
}
