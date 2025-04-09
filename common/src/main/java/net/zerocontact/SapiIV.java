package net.zerocontact;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

//ArmorItem的实现需要盔甲材料类的各种属性与基础的防御值，也是从材料里拿
public class SapiIV extends ArmorItem  {
    protected final Type type;
    protected final ArmorMaterial material;
    protected final int defense;
    protected static final int DAMAGE_PLATE_MULTIPLIER = 1;
    public static final int MAX_HURT_DAMAGE_CAN_HOLD = 7;
    public SapiIV(ArmorMaterial armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties.defaultDurability(armorMaterial.getDurabilityForType(type)));
        this.material = armorMaterial;
        this.type = type;
        this.defense = armorMaterial.getDefenseForType(type);

    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public ArmorMaterial getMaterial() {
        return this.material;
    }

    @Override
    public int getDefense() {
        return this.defense;
    }


    @Override
    public EquipmentSlot getEquipmentSlot() {
        return super.getEquipmentSlot();
    }


    @ExpectPlatform
    public static SapiIV create(ArmorMaterial armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        throw new AssertionError();
    }

    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
