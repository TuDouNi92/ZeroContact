package net.zerocontact.client.tooltip;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.cofig.ModConfigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ClientBallisticToolTipComponent implements ClientTooltipComponent {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 88;
    private static final int PLOT_LEFT_PADDING = 30;
    private static final int PLOT_RIGHT_PADDING = 5;
    private static final int PLOT_TOP_PADDING = 15;
    private static final int PLOT_BOTTOM_PADDING = 14;
    private static final int AXIS_COLOR = 0xFF9A9A9A;
    private static final int GRID_COLOR = 0x445F5F5F;
    private static final int LABEL_COLOR = 0xFFB8B8B8;
    private static final int DEFAULT_CURVE_COLOR = 0xFFFFC247;

    private final CaliberVariantDamageHelper.Caliber caliber;
    private @Nullable BallisticTrajectory trajectory = null;

    public ClientBallisticToolTipComponent(BallisticToolTipComponent data) {
        this.caliber = data.caliber();
        @Nullable IGunOperator operator = Optional.ofNullable(Minecraft.getInstance().player)
                .map(IGunOperator::fromLivingEntity).orElse(null);
        if (operator != null) {
            ItemStack handStack = ((LivingEntity) operator).getMainHandItem();
            IGun gun = IGun.getIGunOrNull(handStack);
            if (gun != null) {
                String ammoId = TimelessAPI.getCommonGunIndex(gun.getGunId(handStack)).map(idx -> idx.getGunData().getAmmoId().toString()).orElse("");
                if (ammoId.isEmpty() || !caliber.id().equals(ammoId)) {
                    return;
                }
                this.trajectory = BallisticTrajectory.simulate(caliber, operator);
            }
        }
    }

    @Override
    public int getHeight() {
        if (!ModConfigs.CLIENT.enableTrajectoryTooltip().get() || trajectory == null) {
            trajectory = null;
            return 0;
        }
        return HEIGHT;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return WIDTH;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        if (trajectory == null) return;
        int plotLeft = x + PLOT_LEFT_PADDING;
        int plotTop = y + PLOT_TOP_PADDING;
        int plotWidth = WIDTH - PLOT_LEFT_PADDING - PLOT_RIGHT_PADDING;
        int plotHeight = HEIGHT - PLOT_TOP_PADDING - PLOT_BOTTOM_PADDING;
        int plotRight = plotLeft + plotWidth;
        int plotBottom = plotTop + plotHeight;

        guiGraphics.drawString(
                font,
                Component.translatable("tooltip.zerocontact.ballistic_chart"),
                x,
                y + 2,
                LABEL_COLOR,
                false
        );

        renderGrid(guiGraphics, plotLeft, plotTop, plotWidth, plotHeight);
        guiGraphics.fill(plotLeft, plotTop, plotLeft + 1, plotBottom + 1, AXIS_COLOR);
        guiGraphics.fill(plotLeft, plotTop, plotRight + 1, plotTop + 1, AXIS_COLOR);
        renderTrajectory(guiGraphics, plotLeft, plotTop, plotWidth, plotHeight);
        renderLabels(font, guiGraphics, x, plotLeft, plotTop, plotRight, plotBottom);
    }

    private void renderGrid(GuiGraphics graphics, int left, int top, int width, int height) {
        for (int division = 1; division < 4; division++) {
            int gridX = left + width * division / 4;
            graphics.fill(gridX, top, gridX + 1, top + height + 1, GRID_COLOR);
        }

        for (int division = 1; division < 4; division++) {
            int gridY = top + height * division / 4;
            graphics.fill(left, gridY, left + width + 1, gridY + 1, GRID_COLOR);
        }
    }

    private void renderTrajectory(GuiGraphics graphics, int left, int top, int width, int height) {
        if (trajectory == null) return;
        List<BallisticTrajectory.Sample> samples = trajectory.samples();
        int previousX = left;
        int previousY = top;
        int color = getCurveColor();

        for (int index = 1; index < samples.size(); index++) {
            BallisticTrajectory.Sample sample = samples.get(index);
            int pixelX = left + (int) Math.round(sample.distance() / trajectory.maxDistance() * width);
            int pixelY = top + (int) Math.round(Math.max(0.0, -sample.height()) / trajectory.maxDrop() * height);
            pixelX = Math.min(left + width, Math.max(left, pixelX));
            pixelY = Math.min(top + height, Math.max(top, pixelY));

            drawLine(graphics, previousX, previousY, pixelX, pixelY, color);
            previousX = pixelX;
            previousY = pixelY;
        }
    }

    private void renderLabels(
            Font font,
            GuiGraphics graphics,
            int componentLeft,
            int plotLeft,
            int plotTop,
            int plotRight,
            int plotBottom
    ) {
        if (trajectory == null) return;
        String zeroDistance = "0m";
        String maxDistance = formatMeasurement(trajectory.maxDistance()) + "m";
        String zeroDrop = "0m";
        String maxDrop = "-" + formatMeasurement(trajectory.maxDrop()) + "m";

        graphics.drawString(font, zeroDrop, componentLeft, plotTop - 3, LABEL_COLOR, false);
        graphics.drawString(font, maxDrop, componentLeft, plotBottom - font.lineHeight, LABEL_COLOR, false);
        graphics.drawString(font, zeroDistance, plotLeft, plotBottom + 3, LABEL_COLOR, false);

        for (int division = 1; division < 4; division++) {
            String distanceLabel = formatMeasurement(trajectory.maxDistance() * division / 4.0) + "m";
            int labelCenter = plotLeft + (plotRight - plotLeft) * division / 4;
            graphics.drawString(
                    font,
                    distanceLabel,
                    labelCenter - font.width(distanceLabel) / 2,
                    plotBottom + 3,
                    LABEL_COLOR,
                    false
            );
        }

        graphics.drawString(font, maxDistance, plotRight - font.width(maxDistance), plotBottom + 3, LABEL_COLOR, false);
    }

    private int getCurveColor() {
        int[] rgba = caliber.tracerColor();
        if (rgba == null || rgba.length < 3) {
            return DEFAULT_CURVE_COLOR;
        }

        int red = clampColor(rgba[0]);
        int green = clampColor(rgba[1]);
        int blue = clampColor(rgba[2]);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static String formatMeasurement(double value) {
        if (value >= 100.0) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        if (value >= 10.0) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static void drawLine(GuiGraphics graphics, int startX, int startY, int endX, int endY, int color) {
        int deltaX = Math.abs(endX - startX);
        int stepX = startX < endX ? 1 : -1;
        int deltaY = -Math.abs(endY - startY);
        int stepY = startY < endY ? 1 : -1;
        int error = deltaX + deltaY;
        int x = startX;
        int y = startY;

        while (true) {
            graphics.fill(x, y, x + 1, y + 1, color);
            if (x == endX && y == endY) {
                return;
            }

            int doubledError = error * 2;
            if (doubledError >= deltaY) {
                error += deltaY;
                x += stepX;
            }
            if (doubledError <= deltaX) {
                error += deltaX;
                y += stepY;
            }
        }
    }
}
