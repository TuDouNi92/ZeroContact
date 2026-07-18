package net.zerocontact.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HelmetRender {
    public static class HelmetArmorRender<TItem extends Item & GeoAnimatable & GeoItem> extends GeoArmorRenderer<TItem> {
        public HelmetArmorRender(GeoModel<TItem> model) {
            super(model);
        }

        @Override
        public void prepForRender(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel) {
            this.setAllVisible(true);
            super.prepForRender(entity, stack, slot, baseModel);
        }
    }
    public static class HelmetItemRender<TItem extends Item & GeoAnimatable & GeoItem> extends GeoItemRenderer<TItem> {
        public HelmetItemRender(GeoModel<TItem> model) {
            super(model);
        }
    }
}
