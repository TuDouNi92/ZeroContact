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
import net.zerocontact.api.IGeoCurioItem;
import net.zerocontact.models.GenerateModel;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class AccessoriesRender<T extends Item & IGeoCurioItem> implements ICurioRenderer.HumanoidRender {
    private final ArmorRender<T> render;
    private final T item;

    public AccessoriesRender(T item) {
        this.item = item;
        render = new ArmorRender<>(new GenerateModel<>(item.texture(), item.model(), item.animation()));
        item.setArmorRender(render);
    }

    @Override
    public HumanoidModel<LivingEntity> getModel(ItemStack itemStack, SlotContext slotContext) {
        return render.asCuriosModel();
    }

    @Override
    public ResourceLocation getModelTexture(ItemStack itemStack, SlotContext slotContext) {
        return item.texture();
    }

    @Override
    public void prepareModel(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        HumanoidRender.super.prepareModel(stack, slotContext, poseStack, renderLayerParent, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        if (stack.getItem() instanceof IEquipmentTypeTag tag) {
            if (tag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.UNIFORM_TOP
                    || tag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.ARMBAND
                    || tag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.BACKPACK
                    || tag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.RIGS
            ) {
                render.prepForRender(slotContext.entity(), stack, EquipmentSlot.CHEST, (HumanoidModel<?>) renderLayerParent.getModel());
            }
            if(tag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.MASK){
                render.prepForRender(slotContext.entity(), stack, EquipmentSlot.HEAD, (HumanoidModel<?>) renderLayerParent.getModel());
            }
            if (tag.getArmorType() == net.zerocontact.api.IEquipmentTypeTag.EquipmentType.UNIFORM_PANTS) {
                render.prepForRender(slotContext.entity(), stack, EquipmentSlot.LEGS, (HumanoidModel<?>) renderLayerParent.getModel());
            }
        }
    }
}
