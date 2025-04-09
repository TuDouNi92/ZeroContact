package net.zerocontact.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.ModSoundEvents;
import net.zerocontact.SapiIV;
import net.zerocontact.forge.models.JpcRender;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;
import java.util.function.Consumer;

//ArmorItem的实现需要盔甲材料类的各种属性与基础的防御值，也是从材料里拿
public class SapiIVImpl extends SapiIV implements ICurioItem, GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public SapiIVImpl(ArmorMaterial armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties.defaultDurability(armorMaterial.getDurabilityForType(type)));
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(ModSoundEvents.ARMOR_EQUIP_PLATE, 1.5f, 1.0f);
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private JpcRender renderer;
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if(this.renderer ==null){
                    this.renderer = new JpcRender();
                }
                this.renderer.prepForRender(livingEntity,itemStack,equipmentSlot,original);
                return this.renderer;
            }
        });
    }
}
