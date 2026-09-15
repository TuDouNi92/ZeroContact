package net.zerocontact.armor.modular.module.nvg.event;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.client.renderer.ThermalBufferSource;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4f;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ThermalRenderHandler {
    public static final float DEFAULT_HEAT = 0.85F;
    // Dedicated to this pass; getBuffer() may flush when the material changes.
    public static final ThermalBufferSource HEAT_BUFFERS = new ThermalBufferSource();
    private static final ThermalBufferSource HAND_HEAT_BUFFERS = new ThermalBufferSource();
    private static WorldMatrices worldMatrices;

    private record WorldMatrices(PoseStack pose, Matrix4f projection, Matrix4f modelView) { }

    /** Use the live hand matrices; Oculus may render hands before the world mask. */
    public static void renderHandHeat(PoseStack poseStack, int light, AbstractClientPlayer player,
                                      ModelPart arm, ModelPart sleeve) {
        renderHandHeat(buffers -> {
            arm.render(poseStack, buffers.getBuffer(
                    RenderType.entitySolid(player.getSkinTextureLocation())), light, OverlayTexture.NO_OVERLAY);
            sleeve.render(poseStack, buffers.getBuffer(
                    RenderType.entityTranslucent(player.getSkinTextureLocation())), light, OverlayTexture.NO_OVERLAY);
        });
    }

    /** Draw the actual first-person geometry, including replacements from RenderArmEvent. */
    public static void renderHandHeat(Consumer<ThermalBufferSource> drawHand) {
        RenderTarget heatTarget = ClientNvgTickHandler.getThermalTarget();
        if (heatTarget == null) return;

        int drawTarget = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readTarget = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        float[] color = RenderSystem.getShaderColor().clone();
        ShaderInstance shader = RenderSystem.getShader();
        int texture = RenderSystem.getShaderTexture(0);
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        try {
            // Use depth from the current hand stage, including Oculus' early hand pass.
            // Never clear heat color here: other hands/world geometry may already exist.
            heatTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            heatTarget.bindWrite(true);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            HAND_HEAT_BUFFERS.setHeat(DEFAULT_HEAT);
            try {
                drawHand.accept(HAND_HEAT_BUFFERS);
            } finally {
                // Material changes can also flush: the target must cover the entire pass.
                HAND_HEAT_BUFFERS.endBatch();
            }
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readTarget);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawTarget);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.depthMask(depthWrite);
            RenderSystem.depthFunc(depthFunc);
            if (depthTest) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        worldMatrices = null;
        RenderTarget heatTarget = ClientNvgTickHandler.getThermalTarget();
        if (heatTarget == null) return;

        // Clear once, BEFORE any world or hand rendering. Oculus renders solid hands
        // inside LevelRenderer, before AFTER_LEVEL; clearing there erases their heat.
        int drawTarget = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readTarget = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        try {
            heatTarget.setClearColor(0, 0, 0, 0);
            heatTarget.clear(Minecraft.ON_OSX);
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readTarget);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawTarget);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.depthMask(depthWrite);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            worldMatrices = null;
            if (ClientNvgTickHandler.getThermalTarget() == null) return;
            PoseStack pose = new PoseStack();
            pose.last().pose().set(event.getPoseStack().last().pose());
            pose.last().normal().set(event.getPoseStack().last().normal());
            worldMatrices = new WorldMatrices(pose, new Matrix4f(event.getProjectionMatrix()),
                    new Matrix4f(RenderSystem.getModelViewMatrix()));
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            // Forge 1.20.1 passes its projection PoseStack at AFTER_LEVEL, not
            // the world view PoseStack. Use this frame's entity-stage snapshot.
            WorldMatrices matrices = worldMatrices;
            worldMatrices = null;
            if (matrices == null || Minecraft.getInstance().level == null) return;
            RenderTarget heatTarget = ClientNvgTickHandler.getThermalTarget();
            if (heatTarget != null) renderHeatMask(event, heatTarget, matrices);
        }
    }

    private static void renderHeatMask(RenderLevelStageEvent event, RenderTarget heatTarget,
                                       WorldMatrices matrices) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget mainTarget = mc.getMainRenderTarget();
        float[] color = RenderSystem.getShaderColor().clone();
        ShaderInstance shader = RenderSystem.getShader();
        int texture = RenderSystem.getShaderTexture(0);
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting sorting = RenderSystem.getVertexSorting();
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();

        try {
            modelView.setIdentity();
            modelView.mulPoseMatrix(matrices.modelView());
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(matrices.projection(), VertexSorting.DISTANCE_TO_ORIGIN);
            // The post chain owns this target and handles resizing/reload/cleanup.
            // It samples the mask later, when GameRenderer processes the chain.
            // Append to this frame's mask: Oculus may already have written hand heat.
            heatTarget.copyDepthFrom(mainTarget);
            heatTarget.bindWrite(true);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            HEAT_BUFFERS.setHeat(DEFAULT_HEAT);
            try {
                renderHeatSources(event, matrices.pose());
            } finally {
                // Flush even on a renderer failure, while the heat target is bound.
                HEAT_BUFFERS.endBatch();
            }
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);
            mainTarget.bindWrite(true);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.depthMask(depthWrite);
            RenderSystem.depthFunc(depthFunc);
            if (depthTest) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        }
    }

    private static void renderHeatSources(RenderLevelStageEvent event, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        float partialTick = event.getPartialTick();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!isHeatSource(entity) || entity.isRemoved() || entity.isSpectator()) continue;
            if (entity == camera.getEntity() && !camera.isDetached()) continue;
            if (!dispatcher.shouldRender(entity, event.getFrustum(), cameraPos.x, cameraPos.y, cameraPos.z)) continue;

            double x = Mth.lerp(partialTick, entity.xOld, entity.getX()) - cameraPos.x;
            double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) - cameraPos.y;
            double z = Mth.lerp(partialTick, entity.zOld, entity.getZ()) - cameraPos.z;
            float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            poseStack.pushPose();
            try {
                dispatcher.render(entity, x, y, z, yaw, partialTick,
                        poseStack, HEAT_BUFFERS, LightTexture.FULL_BRIGHT);
            } finally {
                poseStack.popPose();
            }
        }
    }

    private static boolean isHeatSource(Entity entity) {
        return entity instanceof LivingEntity || entity instanceof EntityKineticBullet;
    }
}
