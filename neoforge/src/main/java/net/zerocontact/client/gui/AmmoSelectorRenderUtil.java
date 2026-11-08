package net.zerocontact.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class AmmoSelectorRenderUtil {
    public record Ring(float centerX, float centerY, float innerRadius, float outerRadius, int segmentCount, float gapDegrees){
    }
    public static void drawSegmentedRing(
            GuiGraphics guiGraphics,
            Ring ring,
            int smoothnessPerSegment,
            int color
    ) {
        if (ring.segmentCount <= 0) {
            return;
        }

        Matrix4f matrix = guiGraphics.pose().last().pose();

        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        double fullCircle = Math.PI * 2.0;
        double sectionAngle = fullCircle / ring.segmentCount;
        double gapAngle = Math.toRadians(ring.gapDegrees);

        if (gapAngle >= sectionAngle) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        double baseAngle = -Math.PI / 2.0;

        for (int segment = 0; segment < ring.segmentCount; segment++) {
            double startAngle = baseAngle + segment * sectionAngle + gapAngle / 2.0;
            double endAngle = baseAngle + (segment + 1) * sectionAngle - gapAngle / 2.0;

            drawRingSection(
                    buffer,
                    matrix,
                    ring,
                    startAngle,
                    endAngle,
                    smoothnessPerSegment,
                    red,
                    green,
                    blue,
                    alpha
            );
        }

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawSegmentedRingPart(
            GuiGraphics guiGraphics,
            Ring ring,
            int segmentIndex,
            int smoothnessPerSegment,
            int color
    ) {
        if (segmentIndex < 0 || segmentIndex >= ring.segmentCount) {
            return;
        }

        Matrix4f matrix = guiGraphics.pose().last().pose();

        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        double fullCircle = Math.PI * 2.0;
        double sectionAngle = fullCircle / ring.segmentCount;
        double gapAngle = Math.toRadians(ring.gapDegrees);

        if (gapAngle >= sectionAngle) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        double baseAngle = -Math.PI / 2.0;
        double startAngle = baseAngle + segmentIndex * sectionAngle + gapAngle / 2.0;
        double endAngle = baseAngle + (segmentIndex + 1) * sectionAngle - gapAngle / 2.0;

        drawRingSection(
                buffer,
                matrix,
                ring,
                startAngle,
                endAngle,
                smoothnessPerSegment,
                red,
                green,
                blue,
                alpha
        );

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawRingSection(
            BufferBuilder buffer,
            Matrix4f matrix,
            Ring ring,
            double startAngle,
            double endAngle,
            int steps,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        steps = Math.max(1, steps);

        for (int i = 0; i < steps; i++) {
            double angle1 = startAngle + (endAngle - startAngle) * i / steps;
            double angle2 = startAngle + (endAngle - startAngle) * (i + 1) / steps;

            float outerX1 = ring.centerX + (float) Math.cos(angle1) * ring.outerRadius;
            float outerY1 = ring.centerY + (float) Math.sin(angle1) * ring.outerRadius;
            float outerX2 = ring.centerX + (float) Math.cos(angle2) * ring.outerRadius;
            float outerY2 = ring.centerY + (float) Math.sin(angle2) * ring.outerRadius;

            float innerX1 = ring.centerX + (float) Math.cos(angle1) * ring.innerRadius;
            float innerY1 = ring.centerY + (float) Math.sin(angle1) * ring.innerRadius;
            float innerX2 = ring.centerX + (float) Math.cos(angle2) * ring.innerRadius;
            float innerY2 = ring.centerY + (float) Math.sin(angle2) * ring.innerRadius;

            buffer.vertex(matrix, outerX1, outerY1, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, outerX2, outerY2, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, innerX1, innerY1, 0).color(red, green, blue, alpha).endVertex();

            buffer.vertex(matrix, outerX2, outerY2, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, innerX2, innerY2, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, innerX1, innerY1, 0).color(red, green, blue, alpha).endVertex();
        }
    }
    public static int getHoveredSegment(
            double mouseX,
            double mouseY,
            Ring ring
    ) {
        double dx = mouseX - ring.centerX;
        double dy = mouseY - ring.centerY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < ring.innerRadius || distance > ring.outerRadius) {
            return -1;
        }

        double angle = Math.atan2(dy, dx);

        double baseAngle = -Math.PI / 2.0;
        angle = normalizeAngle(angle - baseAngle);

        double fullCircle = Math.PI * 2.0;
        double sectionAngle = fullCircle / ring.segmentCount;
        double gapAngle = Math.toRadians(ring.gapDegrees);

        int index = (int) Math.floor(angle / sectionAngle);

        if (index < 0 || index >= ring.segmentCount) {
            return -1;
        }

        double localAngle = angle - index * sectionAngle;

        if (localAngle < gapAngle / 2.0 || localAngle > sectionAngle - gapAngle / 2.0) {
            return -1;
        }

        return index;
    }

    private static double normalizeAngle(double angle) {
        double fullCircle = Math.PI * 2.0;

        angle %= fullCircle;

        if (angle < 0.0) {
            angle += fullCircle;
        }

        return angle;
    }
}
