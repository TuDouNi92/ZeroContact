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
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.models.GenerateModel;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.*;
import java.util.function.Consumer;

public class GenerateArmorGeoImpl extends BaseArmorGeoImpl implements GeoItem, IEquipmentTypeTag {
    protected final int defaultDurability;
    public final Set<GenerationRecord<?>> items = new HashSet<>();
    private final float bluntFactor;
    private final float penetrateFactor;
    private final float ricochetFactor;

    public GenerateArmorGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, float bluntFactor, float penetrateFactor, float ricochetFactor) {
        super(type, id, defense, defaultDurability, absorb, bluntFactor, penetrateFactor, mass, texture, model, animation);
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.CHEST) return super.getAttributeModifiers(slot, stack);
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        modifierMultimap.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor").getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
        return modifierMultimap;
    }

}
