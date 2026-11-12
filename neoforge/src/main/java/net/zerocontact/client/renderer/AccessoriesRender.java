package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.api.IGeoCurioItem;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class AccessoriesRender<E extends Item & IGeoCurioItem> implements ICurioRenderer {
    public final ArmorRender<E> render;
    private final E item;

    public AccessoriesRender(E item) {
        this.item = item;
        render = new ArmorRender<>(new GenerateModel<>(item.getTexture(), item.getModel(), item.getAnimation()));
    }

    @Override
    public <T extends LivingEntity,
            M extends EntityModel<T>>
    void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack matrixStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light, float limbSwing,
            float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw,
            float headPitch) {

        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> baseModel)) {
            return;
        }

        prepareModel(stack, slotContext, baseModel, renderTypeBuffer, limbSwing, limbSwingAmount, partialTicks, netHeadYaw, headPitch);
        RenderType type = render.getRenderType(item, render.getTextureLocation(item), renderTypeBuffer, partialTicks);
        VertexConsumer vertexConsumer = type == null ? null : ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, type, stack.hasFoil());
        render.defaultRender(
                matrixStack,
                item,
                renderTypeBuffer,
                type,
                vertexConsumer,
                0,
                partialTicks,
                light
                );
    }

    private void prepareModel(ItemStack stack, SlotContext slotContext, HumanoidModel<?> baseModel, MultiBufferSource renderTypeBuffer, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch) {
        EquipmentSlot equipmentSlot = getEquipmentSlot(stack);

        // Curios supplies the already posed entity model; GeckoLib uses it to align the armor bones.
        render.prepForRender(
                slotContext.entity(),
                stack,
                equipmentSlot,
                baseModel,
                renderTypeBuffer,
                partialTicks,
                limbSwing,
                limbSwingAmount,
                netHeadYaw,
                headPitch
        );
    }

    public @NotNull EquipmentSlot getEquipmentSlot(ItemStack stack) {
        EquipmentSlot equipmentSlot = item instanceof ArmorItem armorItem ? armorItem.getEquipmentSlot() : EquipmentSlot.CHEST;
        if (stack.getItem() instanceof IEquipmentTypeTag tag) {
            equipmentSlot = switch (tag.getArmorType()) {
                case HELMET, MASK -> EquipmentSlot.HEAD;
                case UNIFORM_PANTS -> EquipmentSlot.LEGS;
                case UNIFORM_TOP, ARMBAND, BACKPACK, RIGS -> EquipmentSlot.CHEST;
                default -> equipmentSlot;
            };
        }
        return equipmentSlot;
    }
}
