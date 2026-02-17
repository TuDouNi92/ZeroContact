package net.zerocontact.item.forge;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.item.Ceramic;
import org.jetbrains.annotations.NotNull;

public class CeramicImpl extends Ceramic implements ICombatArmorItem, PlateInfoProvider {
    private final int defense;
    private final int absorb;
    private final float mass;
    private final Type type;
    private final ArmorMaterial material;

    public CeramicImpl(ArmorMaterial material, Type type, Properties properties, int defense, int absorb, float mass) {
        super(material, type, properties);
        this.defense = defense;
        this.absorb = absorb;
        this.mass = mass;
        this.type = type;
        this.material = material;
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

    public static Ceramic create(ArmorMaterial material, Type type, Properties properties, int defense, int absorb, float mass) {
        return new CeramicImpl(material, type, properties, defense, absorb, mass);
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return (int) Math.round(0.4 * Math.pow(damageAmount * durabilityLossFactor, 1.5) * (1 + hits * 0.5f));
    }

    @Override
    public float generatePenetrated() {
        return 0.5f;
    }

    @Override
    public float generateBlunt() {
        return 0.5f;
    }

    public float getMass() {
        return this.mass;
    }

    @Override
    public int getAbsorb() {
        return this.absorb;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return PlateInfoProvider.super.canEquip(stack,armorType,entity);
    }
}
