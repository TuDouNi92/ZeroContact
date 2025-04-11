package net.zerocontact.item.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.registries.ModSoundEventsReg;
import net.zerocontact.item.SapiIV;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

//ArmorItem的实现需要盔甲材料类的各种属性与基础的防御值，也是从材料里拿
public class SapiIVImpl extends SapiIV implements ICurioItem {
    public SapiIVImpl(ArmorMaterial armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties.defaultDurability(armorMaterial.getDurabilityForType(type)));
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(ModSoundEventsReg.ARMOR_EQUIP_PLATE, 1.5f, 1.0f);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player) {
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        stack.getOrCreateTag().putInt("damage_plate_multiplier", DAMAGE_PLATE_MULTIPLIER);
        if (slotContext.entity() instanceof Player) {
            modifiers.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor" + uuid).getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.nameUUIDFromBytes(("ArmorToughness" + uuid).getBytes()), "CuriosArmorToughness", this.material.getToughness(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public static SapiIV create(ArmorMaterial armorMaterial, Type type, Properties properties) {
        return new SapiIVImpl(armorMaterial, type, properties);
    }
}
