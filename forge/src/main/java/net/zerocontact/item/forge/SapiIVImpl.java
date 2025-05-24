package net.zerocontact.item.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
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


    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        stack.getOrCreateTag().putInt("absorb", getAbsorb());
        if (slotContext.entity() instanceof Player) {
            modifiers.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor" + uuid).getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.nameUUIDFromBytes(("ArmorToughness" + uuid).getBytes()), "CuriosArmorToughness", this.material.getToughness(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.MOVEMENT_SPEED,new AttributeModifier(UUID.nameUUIDFromBytes(("MoveSpeed"+uuid).getBytes()),"MoveSpeed",mass, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return modifiers;
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
}
