package net.zerocontact.item.forge;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.item.SapiIV;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GenerateImpl extends SapiIV implements DurabilityLossProvider, PlateInfoProvider, EntityHurtProvider {
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
    public static Set<GenerateImpl> items = new HashSet<>();

    public GenerateImpl(String id, float bluntDamage,float penetrateDamage, float ricochetDamage, int defense, int absorb, float movementFix, int durabilityLossProvider) {
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

    public static void regItems() {
        ItemLoader.loadFromJson();
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data : itemGenDataList) {
            String name = data.languageName;
            String id = data.id;
            float bluntDamage = data.hurtModifier.bluntMultiplier;
            float penetrateDamage = data.hurtModifier.penetrateMultiplier;
            float ricochetDamage = data.hurtModifier.ricochetMultiplier;
            int defense = data.defense;
            int absorb = data.absorb;
            float movementFix = data.movementFix;
            int durabilityLossProvider = data.durabilityLossModifier;
            items.add(new GenerateImpl(id,bluntDamage,penetrateDamage,ricochetDamage,defense,absorb,movementFix,durabilityLossProvider));
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
}
