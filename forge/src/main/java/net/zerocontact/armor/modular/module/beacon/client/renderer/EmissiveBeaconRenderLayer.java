package net.zerocontact.armor.modular.module.beacon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.beacon.container.BeaconContainer;
import net.zerocontact.armor.modular.module.beacon.item.Beacon;
import net.zerocontact.capability.CapabilityRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EmissiveBeaconRenderLayer<T extends Item & GeoItem & GeoAnimatable> extends GeoRenderLayer<T> {
    private static final ResourceLocation GLOW_TEXTURE =
            new ResourceLocation(
                    ZeroContact.MOD_ID,
                    "textures/models/beacon/beacon.png"
            );
    RenderType glowType =
            RenderType.entityTranslucentEmissive(GLOW_TEXTURE);

    public EmissiveBeaconRenderLayer(GeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }


    @Override
    public void render(
            PoseStack poseStack, T animatable, BakedGeoModel bakedModel,
            RenderType renderType, MultiBufferSource bufferSource,
            VertexConsumer buffer, float partialTick,
            int packedLight, int packedOverlay) {

        if (!(getRenderer() instanceof GeoItemRenderer<?> itemRenderer)) {
            return;
        }

        ItemStack stack = itemRenderer.getCurrentItemStack();
        if (stack == null || stack.isEmpty()
                || !(stack.getItem() instanceof Beacon)) {
            return;
        }

        boolean[] lightOn = {false};
        float[] r = {1}, g = {1}, b = {1};
        stack.getCapability(CapabilityRegistries.BEACON).ifPresent(beacon -> {
            lightOn[0] = beacon.getLightOn();
            switch (beacon.getMode()) {
                case RED -> {
                    r[0] = 1;
                    g[0] = 0;
                    b[0] = 0;
                }
                case BLUE -> {
                    r[0] = 0;
                    g[0] = 0.5f;
                    b[0] = 1;
                }
                case GREEN -> {
                    r[0] = 0;
                    g[0] = 1;
                    b[0] = 0;
                }
                default -> {
                    r[0] = 1;
                    g[0] = 1;
                    b[0] = 1;
                }
            }
        });

        if (!lightOn[0]) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        int periodTicks = 20;
        double phase = (level.getGameTime() % periodTicks) + partialTick;
        boolean flashing = phase < 2 || (phase >= 4 && phase < 6);
        if (!flashing) return;
        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowType,
                bufferSource.getBuffer(glowType),
                partialTick,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                r[0], g[0], b[0], 0.87f
        );
    }

}
