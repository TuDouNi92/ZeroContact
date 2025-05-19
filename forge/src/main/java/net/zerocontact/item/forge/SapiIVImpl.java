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
import net.zerocontact.events.PlateInteract;
import net.zerocontact.events.ProtectionLevelHelper;
import net.zerocontact.registries.ModSoundEventsReg;
import net.zerocontact.item.SapiIV;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

//ArmorItem的实现需要盔甲材料类的各种属性与基础的防御值，也是从材料里拿
public class SapiIVImpl extends SapiIV implements ICurioItem, DurabilityLossProvider, EntityHurtProvider {
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
    public ICurio.@NotNull SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(ModSoundEventsReg.ARMOR_EQUIP_PLATE, 1.5f, 1.0f);
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

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if(PlateInteract.isPlateArmorEquipped(slotContext.entity()))return true;
        return false;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        PlateInteract.onArmorUnequip(slotContext,stack);
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
