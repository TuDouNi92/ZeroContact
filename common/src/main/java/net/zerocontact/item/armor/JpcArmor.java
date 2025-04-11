package net.zerocontact.item.armor;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;

public class JpcArmor extends ArmorItem{

    protected final Type type;
    protected final ArmorMaterial material;
    protected final int defense;
    public JpcArmor(Type type, ArmorMaterial material, Properties properties) {
        super(material,type,properties.defaultDurability(material.getDurabilityForType(type)));
        this.type = type;
        this.material = material;
        this.defense = 2;
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
        return this.defense;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ModSoundEventsReg.ARMOR_EQUIP_PLATE;
    }

    @ExpectPlatform
    public static JpcArmor create(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties){
        throw new AssertionError();
    }
}
