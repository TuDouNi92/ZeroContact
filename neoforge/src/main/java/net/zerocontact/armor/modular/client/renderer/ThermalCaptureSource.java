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
                output.addVertex(recorded.getFloat(i), recorded.getFloat(i + 1), recorded.getFloat(i + 2))
                        .setUv(recorded.getFloat(i + 3), recorded.getFloat(i + 4));
            }
        }
    }

    private static final class Recorder implements VertexConsumer {
        private final FloatArrayList vertices;
        private int vertexOffset;

        private Recorder(FloatArrayList vertices) {
            this.vertices = vertices;
        }

        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            vertexOffset = vertices.size();
            vertices.add(x);
            vertices.add(y);
            vertices.add(z);
            // There is no endVertex callback; reserve UVs for setUv to fill in.
            vertices.add(0.0F);
            vertices.add(0.0F);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            vertices.set(vertexOffset + 3, u);
            vertices.set(vertexOffset + 4, v);
            return this;
        }

        @Override public @NotNull VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public @NotNull VertexConsumer setUv1(int u, int v) { return this; }
        @Override public @NotNull VertexConsumer setUv2(int u, int v) { return this; }
        @Override public @NotNull VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
