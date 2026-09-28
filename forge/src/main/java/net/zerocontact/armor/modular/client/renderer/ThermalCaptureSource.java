package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/** Records the geometry of the normal entity render without invoking its renderer twice. */
public final class ThermalCaptureSource {
    private final Map<RenderType, FloatArrayList> vertices = new LinkedHashMap<>();

    public void clear() {
        vertices.clear();
    }

    public MultiBufferSource wrap(MultiBufferSource original) {
        return new CapturingBufferSource(original);
    }

    public static boolean isCapturing(MultiBufferSource source) {
        return source instanceof CapturingBufferSource;
    }

    private final class CapturingBufferSource implements MultiBufferSource {
        private final MultiBufferSource original;

        private CapturingBufferSource(MultiBufferSource original) {
            this.original = original;
        }

        @Override
        public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
            VertexConsumer normal = original.getBuffer(type);
            if (ThermalRenderType.from(type) == null) return normal;
            FloatArrayList recorded = vertices.computeIfAbsent(type, ignored -> new FloatArrayList());
            return VertexMultiConsumer.create(normal, new Recorder(recorded));
        }
    }

    public void replay(ThermalBufferSource target) {
        for (Map.Entry<RenderType, FloatArrayList> entry : vertices.entrySet()) {
            VertexConsumer output = target.getBuffer(entry.getKey());
            FloatArrayList recorded = entry.getValue();
            for (int i = 0; i < recorded.size(); i += 5) {
                output.vertex(recorded.getFloat(i), recorded.getFloat(i + 1), recorded.getFloat(i + 2))
                        .uv(recorded.getFloat(i + 3), recorded.getFloat(i + 4)).endVertex();
            }
        }
    }

    private static final class Recorder implements VertexConsumer {
        private final FloatArrayList vertices;
        private double x, y, z;
        private float u, v;

        private Recorder(FloatArrayList vertices) {
            this.vertices = vertices;
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

        @Override public @NotNull VertexConsumer color(int r, int g, int b, int a) { return this; }
        @Override public @NotNull VertexConsumer overlayCoords(int u, int v) { return this; }
        @Override public @NotNull VertexConsumer uv2(int u, int v) { return this; }
        @Override public @NotNull VertexConsumer normal(float x, float y, float z) { return this; }
        @Override public void defaultColor(int r, int g, int b, int a) { }
        @Override public void unsetDefaultColor() { }

        @Override
        public void endVertex() {
            vertices.add((float) x);
            vertices.add((float) y);
            vertices.add((float) z);
            vertices.add(u);
            vertices.add(v);
        }
    }
}
