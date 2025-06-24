package net.zerocontact.item.armor.forge;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.models.GenerateModel;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class GenerateArmorImpl extends ArmorItem implements GeoItem, ArmorTypeTag {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final Type type;
    private static final ArmorMaterial material = PlateBaseMaterial.ARMOR_STEEL;
    private final int defense;
    protected final int defaultDurability;
    public static Set<GenerateArmorImpl> items = new HashSet<>();
    public final ResourceLocation texture;
    public final ResourceLocation model;
    public final ResourceLocation animation;
    public String id;

    public GenerateArmorImpl(Type type, String id, int defense, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(material, type, new Properties().defaultDurability(defaultDurability));
        this.type = type;
        this.id = id;
        this.defense = defense;
        this.defaultDurability = defaultDurability;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
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
    public @NotNull SoundEvent getEquipSound() {
        return ModSoundEventsReg.ARMOR_EQUIP_PLATE;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender<GenerateArmorImpl> render;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (texture == null || model == null) return this.render;
                if (render == null) {
                    render = new ArmorRender<>(new GenerateModel(texture, model, animation));
                }
                render.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return render;
            }
        });
    }

    public static void regItems() {
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data0 : itemGenDataList) {
            if (!(data0 instanceof ItemGenData.Armor data)) continue;
            String id = data.id;
            int defense = data.defense;
            int defaultDurability = data.defaultDurability;
            ArmorItem.Type equipmentSlotType = getArmorType(data.equipmentSlot);
            if(equipmentSlotType.equals(Type.HELMET))continue;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerateArmorImpl(equipmentSlotType, id, defense, defaultDurability, texture, model, animation));
        }
    }

    private static ArmorItem.Type getArmorType(String equipmentSlot) {
        if (equipmentSlot.equals("HEAD")) {
            return Type.HELMET;
        }
        return Type.CHESTPLATE;
    }
}
