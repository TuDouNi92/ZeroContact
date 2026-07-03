package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.events.EventUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class ArmedRaiderArmorLayer extends ItemArmorGeoLayer<ArmedRaider> {

    protected static final String DEFAULT_ARMOR = "bone10";
    protected static final String ARMOR_BONE = "Body";
    protected static final String HEAD_BONE = "Head";
    protected static final String LEFT_ARM_BONE = "LeftArm";
    protected static final String RIGHT_ARM_BONE = "RightArm";
    protected static final String ARMBAND_CURIO = "armband";

    public ArmedRaiderArmorLayer(GeoRenderer<ArmedRaider> geoRenderer) {
        super(geoRenderer);
    }

    @Override
    public void renderForBone(PoseStack poseStack, ArmedRaider animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (chestplateStack != null && bone.getName().equals(DEFAULT_ARMOR)) {
            bone.setHidden(true);
        }
        super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    @Override
    protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, ArmedRaider animatable) {
        if (bone.getName().equals(ARMOR_BONE)) {
            return EquipmentSlot.CHEST;
        } else if (bone.getName().equals(HEAD_BONE)) {
            return EquipmentSlot.HEAD;
        }

        return super.getEquipmentSlotForBone(bone, stack, animatable);
    }

    @Override
    protected @Nullable ItemStack getArmorItemForBone(GeoBone bone, ArmedRaider animatable) {
        switch (bone.getName()) {
            case ARMOR_BONE -> {
                return this.chestplateStack;
            }
            case HEAD_BONE -> {
                return this.helmetStack;
            }
            case RIGHT_ARM_BONE -> {
                ItemStack stack = EventUtil.getCuriosStackFirst(animatable, ARMBAND_CURIO);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return super.getArmorItemForBone(bone, animatable);
    }

    @Override
    protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, ArmedRaider animatable, HumanoidModel<?> baseModel) {
        return switch (bone.getName()) {
            case HEAD_BONE -> baseModel.head;
            case ARMOR_BONE -> baseModel.body;
            case LEFT_ARM_BONE -> baseModel.leftArm;
            case RIGHT_ARM_BONE -> baseModel.rightArm;
            default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
        };
    }

    @Override
    protected void prepModelPartForRender(PoseStack poseStack, GeoBone bone, ModelPart sourcePart) {
        GeoCube firstCube = bone.getCubes().get(0);
        ModelPart.Cube armorCube = this.getReferenceCubeForModel(bone, sourcePart);
        double armorBoneSizeX = firstCube.size().x();
        double armorBoneSizeY = firstCube.size().y();
        double armorBoneSizeZ = firstCube.size().z();
        double actualArmorSizeX = Math.abs(armorCube.maxX - armorCube.minX);
        double actualArmorSizeY = Math.abs(armorCube.maxY - armorCube.minY);
        double actualArmorSizeZ = Math.abs(armorCube.maxZ - armorCube.minZ);
        float scaleX = (float) (armorBoneSizeX / actualArmorSizeX);
        float scaleY = (float) (armorBoneSizeY / actualArmorSizeY);
        float scaleZ = (float) (armorBoneSizeZ / actualArmorSizeZ);
        sourcePart.setPos(-bone.getPivotX(), -bone.getPivotY(), bone.getPivotZ());
        poseStack.scale(scaleX, scaleY, scaleZ);
        switch (bone.getName()) {
            case ARMOR_BONE -> poseStack.translate(0, -0.73, 0);
            case HEAD_BONE -> {
            }
        }
    }
}
