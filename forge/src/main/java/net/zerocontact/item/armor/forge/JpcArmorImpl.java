package net.zerocontact.item.armor.forge;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.item.armor.JpcArmor;
import net.zerocontact.models.JpcModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;

public class JpcArmorImpl extends JpcArmor implements GeoItem, ArmorTypeTag {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public JpcArmorImpl(Type type, ArmorMaterial material, Properties properties) {
        super(type, material, properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static JpcArmor create(ArmorMaterial material, Type type, Properties properties){
        return new JpcArmorImpl(type,material,properties);
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender<JpcArmorImpl> renderer;
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if(this.renderer ==null){
                    this.renderer = new ArmorRender<>(new JpcModel());
                }
                this.renderer.prepForRender(livingEntity,itemStack,equipmentSlot,original);
                return this.renderer;
            }
        });
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.PLATE_CARRIER;
    }
}
