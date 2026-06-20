package net.zerocontact.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.item.ammo.GenerateAmmo;
import net.zerocontact.item.armband.GenerateUniformArmbandGeoImpl;
import net.zerocontact.item.armor.forge.GenerateArmorGeoImpl;
import net.zerocontact.item.armor.forge.GenerateCarrierGeoImpl;
import net.zerocontact.item.forge.GeneratePlateImpl;
import net.zerocontact.item.helmet.GenerateHelmetGeoImpl;
import net.zerocontact.item.uniform.GenerateUniformPantsGeoImpl;
import net.zerocontact.item.uniform.GenerateUniformTopGeoImpl;

import java.util.*;
import java.util.stream.Collectors;

public class ItemAdapter {
    public record Mapper<T>(T data, IEquipmentTypeTag.EquipmentType type) {
    }

    public static <T> Mapper<?> getMapper(T data) {
        if (data instanceof ItemGenData.Armor armor) {
            Map<String, IEquipmentTypeTag.EquipmentType> convertMap = Arrays
                    .stream(IEquipmentTypeTag.EquipmentType.values())
                    .collect(Collectors.toMap(IEquipmentTypeTag.EquipmentType::getTypeId, type -> type));
            return new Mapper<>(armor, convertMap.get(armor.equipmentSlot));

        } else if (data instanceof ItemGenData.Plate plate) {
            return new Mapper<>(plate, IEquipmentTypeTag.EquipmentType.PLATE);
        } else if (data instanceof ExperimentalBallisticData ammo) {
            return new Mapper<>(ammo, IEquipmentTypeTag.EquipmentType.AMMO);
        }
        return new Mapper<>(null, null);
    }

    public static final List<? extends IAssetManager.GeneratableItem> ADAPTERS = List.of(
            new ArmorAdapter(),
            new PlateAdapter(),
            new CarrierAdapter(),
            new HelmetAdapter(),
            new ArmbandAdapter(),
            new UniformTopAdapter(),
            new UniformPantsAdapter(),
            new AmmoAdapter()
    );

    public static class ArmorAdapter implements IAssetManager.GeneratableItem {
        @Override
        public <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.ARMOR) return items;
            String id = data.id;
            int defense = data.defense;
            int defaultDurability = data.defaultDurability;
            int absorb = data.protectionClass;
            int mass = 0;
            float bluntFactor = data.hurtModifier.bluntMultiplier;
            float penetratedFactor = data.hurtModifier.penetrateMultiplier;
            float ricochetFactor = data.hurtModifier.ricochetMultiplier;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord<>(id, new GenerateArmorGeoImpl(ArmorItem.Type.CHESTPLATE, id, defense, defaultDurability, absorb, mass, texture, model, animation, bluntFactor, penetratedFactor, ricochetFactor), tab));
            return items;
        }
    }

    public static class PlateAdapter implements IAssetManager.GeneratableItem {

        @Override
        public <T extends ItemGenData.Plate> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.PLATE) return items;
            String id = data.id;
            float bluntDamage = data.hurtModifier.bluntMultiplier;
            float penetrateDamage = data.hurtModifier.penetrateMultiplier;
            float ricochetDamage = data.hurtModifier.ricochetMultiplier;
            int defense = data.defense;
            int absorb = data.protectionClass;
            float movementFix = data.movementFix;
            int durabilityLossProvider = data.durabilityLossModifier;
            int durability = data.durability;
            items.add(new GenerationRecord<>(id, new GeneratePlateImpl(id, durability, bluntDamage, penetrateDamage, ricochetDamage, defense, absorb, movementFix, durabilityLossProvider), tab));
            return items;
        }
    }

    public static class CarrierAdapter implements IAssetManager.GeneratableItem {

        @Override
        public <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.PLATE_CARRIER) return items;
            String id = data.id;
            int defense = data.defense;
            int defaultDurability = data.defaultDurability;
            int absorb = data.protectionClass;
            int mass = 0;
            float penetrateReduction = data.hurtModifier.penetrateMultiplier;
            float bluntReduction = data.hurtModifier.bluntMultiplier;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord<>(id, new GenerateCarrierGeoImpl(ArmorItem.Type.CHESTPLATE, id, defense, defaultDurability, absorb, bluntReduction, penetrateReduction, mass, texture, model, animation), tab));
            return items;
        }
    }

    public static class HelmetAdapter implements IAssetManager.GeneratableItem {
        @Override
        public <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.HELMET) return items;
            String id = data.id;
            float bluntDamage = data.hurtModifier.bluntMultiplier;
            float penetrateDamage = data.hurtModifier.penetrateMultiplier;
            float ricochetDamage = data.hurtModifier.ricochetMultiplier;
            int defense = data.defense;
            int absorb = data.protectionClass;
            int durabilityLossProvider = data.durabilityLossModifier;
            int default_durability = data.defaultDurability;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord<>(id, new GenerateHelmetGeoImpl(id, ArmorItem.Type.HELMET, texture, model, animation, defense, absorb, bluntDamage, penetrateDamage, ricochetDamage, durabilityLossProvider, default_durability), tab));
            return items;
        }
    }

    public static class UniformTopAdapter implements IAssetManager.GeneratableItem {

        @Override
        public <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.UNIFORM_TOP) return items;
            String id = data.id;
            int defaultDurability = data.defaultDurability;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord<>(id, new GenerateUniformTopGeoImpl(id, defaultDurability, texture, model, animation), tab));
            return items;
        }
    }

    public static class UniformPantsAdapter implements IAssetManager.GeneratableItem {

        @Override
        public <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.UNIFORM_PANTS) return items;
            String id = data.id;
            int defaultDurability = data.defaultDurability;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord<>(id, new GenerateUniformPantsGeoImpl(id, defaultDurability, texture, model, animation), tab));
            return items;
        }
    }

    public static class ArmbandAdapter implements IAssetManager.GeneratableItem {

        @Override
        public <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (getMapper(data).type != IEquipmentTypeTag.EquipmentType.ARMBAND) return items;
            String id = data.id;
            int defaultDurability = data.defaultDurability;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord<>(id, new GenerateUniformArmbandGeoImpl(id, defaultDurability, texture, model, animation), tab));
            return items;
        }
    }

    public static class AmmoAdapter implements IAssetManager.GeneratableItem {
        @Override
        public <T> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            LinkedHashSet<GenerationRecord<?>> items = new LinkedHashSet<>();
            if (!(data instanceof ExperimentalBallisticData ammoData)) return items;
            String id = ammoData.ammoId;
            String variant = ammoData.variant;
            float baseDamageFactor = ammoData.baseDamageFactor;
            int penetrationClass = ammoData.penetrationClass;
            float fleshDamage = ammoData.fleshDamage;
            float armorDamage = ammoData.armorDamage;
            int stackSize = ammoData.stackSize;
            items.add(new GenerationRecord<>(variant, new GenerateAmmo(id, variant, baseDamageFactor, penetrationClass, fleshDamage, armorDamage, stackSize), tab));
            return items;
        }
    }
}
