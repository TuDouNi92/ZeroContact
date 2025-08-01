package net.zerocontact.item.forge;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.item.SapiIV;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

 public class GeneratePlateImpl extends SapiIV implements DurabilityLossProvider, PlateInfoProvider, EntityHurtProvider {
    private final int defense;
    private final int absorb;
    private final float movementFix;
    private static final ArmorItem.Type type = ArmorItem.Type.CHESTPLATE;
    private static final ArmorMaterial material = PlateBaseMaterial.ARMOR_STEEL;
    private final float bluntDamage;
    private final float penetrateDamage;
    private final float ricochetDamage;
    private final int durabilityLossProvider;
    public String id;
    public static Set<GenerationRecord> items = new HashSet<>();

    public GeneratePlateImpl(String id, float bluntDamage, float penetrateDamage, float ricochetDamage, int defense, int absorb, float movementFix, int durabilityLossProvider) {
        super(material, type, new Properties());
        this.id = id;
        this.defense = defense;
        this.absorb = absorb;
        this.movementFix = movementFix;
        this.bluntDamage = bluntDamage;
        this.penetrateDamage = penetrateDamage;
        this.ricochetDamage = ricochetDamage;
        this.durabilityLossProvider = durabilityLossProvider;
    }

    public static void deserializeItems() {
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data0 : itemGenDataList) {
            if(!(data0 instanceof ItemGenData.Plate data))continue;
            String id = data.id;
            float bluntDamage = data.hurtModifier.bluntMultiplier;
            float penetrateDamage = data.hurtModifier.penetrateMultiplier;
            float ricochetDamage = data.hurtModifier.ricochetMultiplier;
            int defense = data.defense;
            int absorb = data.absorb;
            float movementFix = data.movementFix;
            int durabilityLossProvider = data.durabilityLossModifier;
            items.add(new GenerationRecord(id,new GeneratePlateImpl(id,bluntDamage,penetrateDamage,ricochetDamage,defense,absorb,movementFix,durabilityLossProvider)));
        }
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }

    @Override
    public int getDefense() {
        return this.defense;
    }

    @Override
    public int getAbsorb() {
        return this.absorb;
    }

    @Override
    public float getMass() {
        return this.movementFix;
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return this.durabilityLossProvider;
    }

    @Override
    public float generateBlunt() {
        return this.bluntDamage;
    }

    @Override
    public float generatePenetrated() {
        return this.penetrateDamage;
    }

    @Override
    public float generateRicochet() {
        return this.ricochetDamage;
    }

     @Override
     public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
         return super.getAttributeModifiers(slot, stack);
     }

     @Override
     public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
         return PlateInfoProvider.super.canEquip(stack, armorType, entity);
     }
 }
