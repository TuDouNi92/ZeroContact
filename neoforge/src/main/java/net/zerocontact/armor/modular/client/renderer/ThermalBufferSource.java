package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
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
    private final boolean hand;
    private int heat = 255;

    public ThermalBufferSource() {
        this(false);
    }

    public ThermalBufferSource(boolean hand) {
        this(MultiBufferSource.immediate(new ByteBufferBuilder(256)), hand);
    }

    public ThermalBufferSource(BufferSource delegate) {
        this(delegate, false);
    }

    private ThermalBufferSource(BufferSource delegate, boolean hand) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.hand = hand;
    }

    /**
     * Red stores heat, blue marks hand heat, and alpha stores coverage.
     */
    public void setHeat(float heat) {
        if (!Float.isFinite(heat)) throw new IllegalArgumentException("Heat must be finite");
        this.heat = Math.round(Math.clamp(heat, 0, 1) * 255);
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        RenderType thermal = ThermalRenderType.from(type);
        return thermal == null ? DISCARD : new HeatVertexConsumer(delegate.getBuffer(thermal), heat, hand);
    }

    public void endBatch() {
        delegate.endBatch();
    }

    // Forward positions and UVs to POSITION_TEX_COLOR and supply the heat color
        // for every vertex. Ignore source tint, lighting, overlay and normals.
        private record HeatVertexConsumer(VertexConsumer delegate, int heat, boolean hand) implements VertexConsumer {
            private HeatVertexConsumer(VertexConsumer delegate, int heat) {
                this(delegate, heat, false);
            }

        @Override
            public @NotNull VertexConsumer addVertex(float x, float y, float z) {
                if (delegate != null) {
                    // Blue identifies hand heat, which must remain visible after the
                    // first-person depth pass replaces the world's depth buffer.
                    delegate.addVertex(x, y, z).setColor(heat, heat, hand ? 255 : 0, 255);
                }
                return this;
            }

            @Override
            public @NotNull VertexConsumer setUv(float u, float v) {
                if (delegate != null) {
                    delegate.setUv(u, v);
                }
                return this;
            }

            @Override
            public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
                return this;
            }

            @Override
            public @NotNull VertexConsumer setUv1(int u, int v) {
                return this;
            }

            @Override
            public @NotNull VertexConsumer setUv2(int u, int v) {
                return this;
            }

            @Override
            public @NotNull VertexConsumer setNormal(float x, float y, float z) {
                return this;
            }
        }
}
