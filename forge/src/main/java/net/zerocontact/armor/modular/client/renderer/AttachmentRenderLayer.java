package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.model.MountDefinition;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.util.RenderUtils;

public class AttachmentRenderLayer<T extends Item & GeoItem & GeoAnimatable> extends BlockAndItemGeoLayer<T> {
    private final GeoRenderer<T> geoRenderer;

    public AttachmentRenderLayer(GeoRenderer<T> renderer) {
        super(renderer);
        this.geoRenderer = renderer;
    }


    @Override
    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        // The renderer is shared; read the equipped stack for this render call.
        ItemStack equipmentStack = ItemStack.EMPTY;
        if (geoRenderer instanceof GeoArmorRenderer<T> armorRenderer) {
            equipmentStack = armorRenderer.getCurrentStack();
        } else if (geoRenderer instanceof GeoItemRenderer<T> itemRenderer) {
            equipmentStack = itemRenderer.getCurrentItemStack();
        }

        if (equipmentStack == null || equipmentStack.isEmpty()
                || !(equipmentStack.getItem() instanceof ModularEquipment equipment)) return;

        try {
            for (MountDefinition mountDefinition : equipment.getMountDefinitions(equipmentStack)) {
                if (!bone.getName().equals(mountDefinition.mountBone())) continue;
                if (geoRenderer instanceof GeoArmorRenderer<?>) {
                    EquipmentAnchorCapture.capture(equipmentStack, mountDefinition, bone, poseStack);
                }
                ItemStack module = ModuleQuery.getMounted(equipmentStack, mountDefinition.mountId())
                        .orElse(ItemStack.EMPTY);
                if (module.isEmpty()) continue;
                poseStack.pushPose();
                try {
                    RenderUtils.translateToPivotPoint(poseStack, bone);
                    super.renderStackForBone(poseStack, bone, module, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            // Item rendering may switch buffers; restore the armor render type.
            bufferSource.getBuffer(renderType);
        }
    }
}
