package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import net.zerocontact.models.GenerateModel;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class AccessoriesRender<T extends Item & GeoItem & GeoAnimatable, E extends AbstractGenerateGeoCurioItemImpl, B extends LivingEntity &GeoAnimatable> implements ICurioRenderer.HumanoidRender {
    private final ArmorRender<T> render;
    private final E item;

    public AccessoriesRender(E item) {
        this.item = item;
        render = new ArmorRender<>(new GenerateModel<>(item.texture, item.model, item.animation));
    }

    @Override
    public HumanoidModel<LivingEntity> getModel(ItemStack itemStack, SlotContext slotContext) {
        return (HumanoidModel<LivingEntity>) render;
    }

    @Override
    public ResourceLocation getModelTexture(ItemStack itemStack, SlotContext slotContext) {
        return item.texture;
    }

    @Override
    public void prepareModel(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        HumanoidRender.super.prepareModel(stack, slotContext, poseStack, renderLayerParent, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        if (stack.getItem() instanceof IEquipmentTypeTag IEquipmentTypeTag) {
            if (IEquipmentTypeTag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.UNIFORM_TOP
                    || IEquipmentTypeTag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.ARMBAND
                    || IEquipmentTypeTag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.BACKPACK
                    || IEquipmentTypeTag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.RIGS
            ) {
                render.prepForRender(slotContext.entity(), stack, EquipmentSlot.CHEST, (HumanoidModel<?>) renderLayerParent.getModel());
            }
            if (IEquipmentTypeTag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.UNIFORM_PANTS) {
                render.prepForRender(slotContext.entity(), stack, EquipmentSlot.LEGS, (HumanoidModel<?>) renderLayerParent.getModel());
            }
        }
    }
}
