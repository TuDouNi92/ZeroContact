package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Entity silhouette buffers. Bind the heat target before requesting buffers and
 * call endBatch() before restoring the main target. The delegate must be dedicated
 * to this pass: changing render types can flush it immediately.
 * Use a white RenderSystem shader color for unmodulated heat and coverage.
 * Supports outline-eligible composite quad materials and vanilla cutout armor;
 * custom materials without outline support are discarded.
 */
public final class ThermalBufferSource implements MultiBufferSource {
    private static final VertexConsumer DISCARD = new HeatVertexConsumer(null, 0);
    private final BufferSource delegate;
    private int heat = 255;

    public ThermalBufferSource() {
        this(MultiBufferSource.immediate(new BufferBuilder(256)));
    }

    public ThermalBufferSource(BufferSource delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /** Heat is stored as grayscale RGB, with opaque coverage in alpha. */
    public void setHeat(float heat) {
        if (!Float.isFinite(heat)) throw new IllegalArgumentException("Heat must be finite");
        this.heat = Math.round(Math.max(0, Math.min(1, heat)) * 255);
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        RenderType thermal = ThermalRenderType.from(type);
        return thermal == null ? DISCARD : new HeatVertexConsumer(delegate.getBuffer(thermal), heat);
    }

    public void endBatch() {
        delegate.endBatch();
    }

    // Buffer positions and UVs so arbitrary source formats/attribute ordering can
    // feed POSITION_COLOR_TEX. Ignore tint, lighting, overlay and normals.
    private static final class HeatVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int heat;
        private double x, y, z;
        private float u, v;

        private HeatVertexConsumer(VertexConsumer delegate, int heat) {
            this.delegate = delegate;
            this.heat = heat;
        }

        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public @NotNull VertexConsumer color(int r, int g, int b, int a) { return this; }
        @Override
        public @NotNull VertexConsumer overlayCoords(int u, int v) { return this; }
        @Override
        public @NotNull VertexConsumer uv2(int u, int v) { return this; }
        @Override
        public @NotNull VertexConsumer normal(float x, float y, float z) { return this; }
        @Override
        public void defaultColor(int r, int g, int b, int a) { }
        @Override
        public void unsetDefaultColor() { }

        @Override
        public void endVertex() {
            if (delegate != null) {
                delegate.vertex(x, y, z).color(heat, heat, heat, 255).uv(u, v).endVertex();
            }
        }
    }
}
