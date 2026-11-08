package net.zerocontact.armor.modular.module.pouch.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Owns the off-screen target. Its registered texture is only a borrowed view of the color attachment.
 */
public final class NavBoardScreenTarget implements AutoCloseable {
    public static final int WIDTH = 256, HEIGHT = 128;
    private final ResourceLocation location;
    private final TextureTarget target;
    private DynamicTexture map;
    private final RenderType renderType;
    private final BufferBuilder vertices = new BufferBuilder(1024);
    private boolean closed;

    public NavBoardScreenTarget(ResourceLocation location) {
        RenderSystem.assertOnRenderThread();
        this.location = location;
        // Allocation also changes framebuffer/texture bindings, so protect it just like drawing.
        try (State ignored = new State()) {
            target = new TextureTarget(WIDTH, HEIGHT, false, Minecraft.ON_OSX);
            map = new DynamicTexture(NavBoardScreens.MAP_SIZE, NavBoardScreens.MAP_SIZE, false);
            map.setFilter(false, false);
            Minecraft.getInstance().getTextureManager().register(location, new AbstractTexture() {
                @Override
                public int getId() {
                    return target.getColorTextureId();
                }

                @Override
                public void load(ResourceManager resources) {
                }

                @Override
                public void releaseId() {
                } // RenderTarget is the sole owner.

                @Override
                public void close() {
                }
            });
        }
        renderType = RenderType.entityTranslucentEmissive(location);
    }

    public RenderType renderType() {
        return renderType;
    }

    public void upload(int[] colors) {
        RenderSystem.assertOnRenderThread();
        int mapSize = (int) Math.sqrt(colors.length);
        if (mapSize == 0 || mapSize * mapSize != colors.length) {
            throw new IllegalArgumentException("Navigation map snapshot must be a nonempty square");
        }
        try (State ignored = new State()) {
            // Hot reload can change the sampling radius without reconstructing this screen.
            // The snapshot, rather than an inlined constant, is the source of truth for allocation.
            if (map.getPixels() == null || map.getPixels().getWidth() != mapSize
                    || map.getPixels().getHeight() != mapSize) {
                map.close();
                map = new DynamicTexture(mapSize, mapSize, false);
                map.setFilter(false, false);
            }
            var pixels = map.getPixels();
            if (pixels == null) return;
            for (int z = 0; z < mapSize; z++) {
                for (int x = 0; x < mapSize; x++) {
                    int color = colors[z * mapSize + x];
                    // MapColor.calculateRGBColor and NativeImage both use ABGR in 1.20.1.
                    pixels.setPixelRGBA(x, z, color == 0 ? 0xff241b14 : color);
                }
            }
            map.upload();
        }
    }

    public void render(ClientLevel level, double playerX, double playerZ,
                       int originX, int originZ, float yaw, float partial) {
        RenderSystem.assertOnRenderThread();
        if (map.getPixels() == null) return;
        int mapSize = map.getPixels().getWidth();
        try (State ignored = new State()) {
            target.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, WIDTH, HEIGHT, 0, -1, 1), VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.applyModelViewMatrix();

            // Use the actual allocated size, including screens created before a radius change.
            double halfWidth = NavBoardScreens.viewWidthBlocks() / 2.0;
            double halfHeight = halfWidth * HEIGHT / WIDTH;
            double mapPlayerX = playerX - originX;
            double mapPlayerZ = playerZ - originZ;
            float u0 = (float) ((mapPlayerX - halfWidth) / mapSize);
            float v0 = (float) ((mapPlayerZ - halfHeight) / mapSize);
            float u1 = (float) ((mapPlayerX + halfWidth) / mapSize);
            float v1 = (float) ((mapPlayerZ + halfHeight) / mapSize);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            vertices.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            colorVertex(0, 0, 20, 27, 36);
            colorVertex(0, HEIGHT, 20, 27, 36);
            colorVertex(WIDTH, HEIGHT, 20, 27, 36);
            colorVertex(WIDTH, 0, 20, 27, 36);
            BufferUploader.drawWithShader(vertices.end());
            // Clip to available sampled terrain instead of stretching edge texels into unknown space.
            float left = Math.max(0, -u0 / (u1 - u0) * WIDTH);
            float right = Math.min(WIDTH, (1 - u0) / (u1 - u0) * WIDTH);
            float top = Math.max(0, -v0 / (v1 - v0) * HEIGHT);
            float bottom = Math.min(HEIGHT, (1 - v0) / (v1 - v0) * HEIGHT);
            if (left < right && top < bottom) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, map.getId());
                vertices.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                texVertex(left, top, Math.max(0, u0), Math.max(0, v0));
                texVertex(left, bottom, Math.max(0, u0), Math.min(1, v1));
                texVertex(right, bottom, Math.min(1, u1), Math.min(1, v1));
                texVertex(right, top, Math.min(1, u1), Math.max(0, v0));
                BufferUploader.drawWithShader(vertices.end());
            }
            // North-up terrain, smoothly rotating player arrow (Minecraft yaw 0 faces south).
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            vertices.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            drawAllyArrow(level, playerX, playerZ, partial);
            drawCenterArrow(yaw);
            BufferUploader.drawWithShader(vertices.end());
        }
    }

    public void drawCenterArrow(float yaw) {
        double radians = Math.toRadians(yaw);
        float dx = (float) -Math.sin(radians), dz = (float) Math.cos(radians);
        centerArrowVertex(dx * 7, dz * 7);
        centerArrowVertex(-dx * 4 - dz * 4, -dz * 4 + dx * 4);
        centerArrowVertex(-dx * 4 + dz * 4, -dz * 4 - dx * 4);
    }

    private void drawAllyArrow(ClientLevel level, double playerX, double playerZ, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            level.players().stream().filter(p -> !p.getUUID().equals(mc.player.getUUID()) && p.isAlliedTo(mc.player))
                    .forEach(p -> {
                        double px = Mth.lerp(partial, p.xo, p.getX());
                        double pz = Mth.lerp(partial, p.zo, p.getZ());
                        float pYaw = Mth.rotLerp(partial, p.yRotO, p.getYRot());
                        double radians = Math.toRadians(pYaw);
                        float dx = (float) -Math.sin(radians);
                        float dz = (float) Math.cos(radians);
                        float pixelsPerBlock = (float) WIDTH / NavBoardScreens.viewWidthBlocks();
                        float centerX = WIDTH / 2f + (float) (px - playerX) * pixelsPerBlock;
                        float centerZ = HEIGHT / 2f + (float) (pz - playerZ) * pixelsPerBlock;
                        colorVertex(centerX + dx * 7,
                                centerZ + dz * 7, 255, 150, 70);
                        colorVertex(centerX - dx * 4 - dz * 4,
                                centerZ - dz * 4 + dx * 4, 255, 150, 70);
                        colorVertex(centerX - dx * 4 + dz * 4,
                                centerZ - dz * 4 - dx * 4, 255, 150, 70);
                    });
        }
    }

    private void texVertex(float x, float y, float u, float v) {
        vertices.vertex(x, y, 0).uv(u, v).endVertex();
    }

    private void colorVertex(float x, float y, int r, int g, int b) {
        vertices.vertex(x, y, 0).color(r, g, b, 255).endVertex();
    }

    private void centerArrowVertex(float x, float y) {
        colorVertex(WIDTH / 2f + x, HEIGHT / 2f + y, 255, 235, 70);
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        if (closed) return;
        closed = true;
        try (State ignored = new State()) {
            Minecraft.getInstance().getTextureManager().release(location);
            map.close();
            target.destroyBuffers();
        }
    }

    /**
     * RenderTick START has no pending model batch. Restore the actual target, including modded targets.
     */
    private static final class State implements AutoCloseable {
        private final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        private final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        private final int[] viewport = new int[4];
        private final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        private final boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        private final java.nio.ByteBuffer colorMask = org.lwjgl.BufferUtils.createByteBuffer(4);
        private final Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        private final VertexSorting sorting = RenderSystem.getVertexSorting();
        private final net.minecraft.client.renderer.ShaderInstance shader = RenderSystem.getShader();
        private final int texture = RenderSystem.getShaderTexture(0);
        private final int activeTexture = GL11.glGetInteger(org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE);
        private final int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        private final int textureZero;
        private final float[] color = RenderSystem.getShaderColor().clone();

        State() {
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMask);
            RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
            textureZero = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            RenderSystem.activeTexture(activeTexture);
            RenderSystem.getModelViewStack().pushPose();
        }

        @Override
        public void close() {
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.setProjectionMatrix(projection, sorting);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
            RenderSystem.bindTexture(textureZero);
            RenderSystem.activeTexture(activeTexture);
            RenderSystem.bindTexture(boundTexture);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            if (depth) RenderSystem.enableDepthTest();
            else RenderSystem.disableDepthTest();
            if (cull) RenderSystem.enableCull();
            else RenderSystem.disableCull();
            if (blend) RenderSystem.enableBlend();
            else RenderSystem.disableBlend();
            if (scissor) com.mojang.blaze3d.platform.GlStateManager._enableScissorTest();
            else RenderSystem.disableScissor();
            RenderSystem.depthMask(depthMask);
            RenderSystem.colorMask(colorMask.get(0) != 0, colorMask.get(1) != 0, colorMask.get(2) != 0, colorMask.get(3) != 0);
        }
    }
}
