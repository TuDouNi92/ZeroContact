package net.zerocontact.item.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.events.ProtectionLevelHelper;
import net.zerocontact.item.SapiIV;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

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
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        Component tipsToAdd = Component.translatable(ProtectionLevelHelper.get(absorb).name());
        if(tooltips.contains(tipsToAdd))return tooltips;
        tooltips.add(tipsToAdd);
        return tooltips;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return super.getAttributeModifiers(slot, stack);
    }
}
