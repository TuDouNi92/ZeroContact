package net.zerocontact.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.zerocontact.client.gui.ConfigScreen;
import net.zerocontact.cofig.ModConfigs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ConfigOptionsList extends ContainerObjectSelectionList<ConfigOptionsList.OptionEntry> {
    public final List<OptionEntry> entries;
    private final ConfigScreen owner;
    private OptionEntry defaultLabel;

    public ConfigOptionsList(ConfigScreen owner, Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.owner = owner;
        this.entries = new ArrayList<>();
    }

    public <T extends OptionEntry> void add(T entry) {
        entries.add(entry);
        addEntry(entry);
        if (entry instanceof Title) {
            setDefaultLabel(defaultLabel);
        } else if (defaultLabel != null) {
            entry.setParentTitle(defaultLabel);
        }
    }

    public void setDefaultLabel(OptionEntry defaultParent) {
        this.defaultLabel = defaultParent;
    }

    @Override
    public int getRowWidth() {
        return this.owner.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.owner.width - 6;
    }

    @Override
    public boolean isFocused() {
        return this.owner.getFocused() == this;
    }

    public static class OptionEntry extends Entry<OptionEntry> {
        private OptionEntry parentTitle;
        protected final Minecraft client = Minecraft.getInstance();
        private final List<AbstractWidget> widgets = new ArrayList<>();
        private final List<Vector2i> widgetOffsets = new ArrayList<>();

        public record WidgetBox(AbstractWidget widget, int offsetX, int offsetY) {

            public static List<WidgetBox> booleanWidget(ConfigOptionsList list, String descriptionKey, Font font, ForgeConfigSpec.BooleanValue booleanValue) {
                WidgetBox buttonBox = new WidgetBox(
                        new Button.Builder(
                                Component.literal(String.valueOf(booleanValue.get())),
                                btn -> {
                                    ModConfigs.flipValue(booleanValue);
                                    btn.setMessage(Component.literal(String.valueOf(booleanValue.get())));
                                }
                        )
                                .width(48)
                                .build(),
                        list.getRowRight() - 64,
                        list.itemHeight / 2
                );
                return List.of(
                        new WidgetBox(
                                new StringWidget(
                                        Component.translatable(descriptionKey),
                                        font
                                ),
                                16,
                                list.itemHeight / 2 + 4
                        ),
                        buttonBox
                );
            }
        }

        public void setParentTitle(OptionEntry parentTitle) {
            this.parentTitle = parentTitle;
        }

        public void addWidget(List<WidgetBox> widgets) {
            widgets.forEach(widget -> {
                this.widgets.add(widget.widget);
                widgetOffsets.add(new Vector2i(widget.offsetX, widget.offsetY));
            });

        }


        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            for (AbstractWidget widget : widgets) {
                Vector2i offsets = widgetOffsets.get(widgets.indexOf(widget));
                widget.setX(left + offsets.x);
                widget.setY(top + offsets.y);
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return this.widgets;
        }

        public OptionEntry getParentTitle() {
            return parentTitle;
        }
    }

    public static class Title extends OptionEntry {
        public final MutableComponent title;

        public Title(MutableComponent title) {
            this.title = title;
        }

        public MutableComponent getTitle() {
            return title;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return super.narratables();
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int textX = getTextX(left, width);
            int textY = top + height - 8;
            guiGraphics.drawString(client.font, title, textX, textY, 0xffffff);
        }

        public int getTextX(int rowLeft, int width) {
            return rowLeft + (width - client.font.width(title)) / 2;
        }
    }
}
