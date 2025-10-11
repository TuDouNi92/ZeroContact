package net.zerocontact.item.forge;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.item.SapiIV;
import org.jetbrains.annotations.NotNull;

public class SapiIVImpl extends SapiIV implements DurabilityLossProvider, EntityHurtProvider, PlateInfoProvider {
    private final Type type;
    private final ArmorMaterial material;
    private final int defense;
    private final int absorb;
    private final float mass;
    public SapiIVImpl(ArmorMaterial material, Type type, Properties properties, int defense, int absorb, float mass) {
        super(material, type, properties.defaultDurability(material.getDurabilityForType(type)));
        this.type = type;
        this.material = material;
        this.defense = defense;
        this.absorb = absorb;
        this.mass = mass;
    }

    public int getAbsorb() {
        return absorb;
    }

    public float getMass() {
        return mass;
    }
    @Override
    public @NotNull Type getType() {
        return this.type;
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return this.material;
    }

    @Override
    public int getDefense() {
        if(this.material==null) return 0;
        return this.defense;
    }

    public static SapiIV create(ArmorMaterial armorMaterial, Type type, Properties properties, int defense, int absorb, float mass) {
        return new SapiIVImpl(armorMaterial, type, properties, defense, absorb,mass);
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
