package net.zerocontact.armor.modular.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.client.widget.EquipmentCard;
import net.zerocontact.armor.modular.client.widget.EquipmentList;
import net.zerocontact.armor.modular.client.renderer.EquipmentAnchorCapture.Anchor;
import net.zerocontact.armor.modular.client.renderer.EquipmentPlayerPreview;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.client.interaction.KeyBindingHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class EquipmentScreen extends AbstractContainerScreen<EquipmentMenu> {
    private static final int MARGIN = 10;
    private static final int ANCHOR_RADIUS = 3;
    private EquipmentList equipmentList;
    private EquipmentCard selectedCard;
    private int displayedRevision = -1;
    private ResourceLocation displayedMountId;
    private EquipmentTarget displayedTarget;
    private List<Anchor> anchors = List.of();
    private int previewLeft, previewTop, previewRight, previewBottom, listLeft;
    private float yaw, pitch, zoom = 1;
    private boolean draggingPreview;

    public EquipmentScreen(EquipmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        imageWidth = width;
        imageHeight = height;
        super.init();
        previewLeft = MARGIN;
        previewRight = Math.max(previewLeft + 1, width / 2 - 4);
        previewTop = 32;
        previewBottom = Math.max(previewTop + 1, height - 30);
        listLeft = previewRight + 8;
        int listWidth = Math.max(1, width - MARGIN - listLeft);
        equipmentList = new EquipmentList(minecraft, listWidth, height, previewTop + 22, previewBottom);
        equipmentList.setLeftPos(listLeft);
        addRenderableWidget(equipmentList);
        displayedRevision = -1;
        draggingPreview = false;
        anchors = List.of();
        refreshCards();
    }

    /**
     * Call from the anchor selection control.
     */
    public void selectMount(ResourceLocation mountId) {
        menu.setMountId(mountId);
    }

    private void refreshCards() {
        if (equipmentList == null || displayedRevision == menu.getCandidateRevision()) return;
        double scroll = equipmentList.getScrollAmount();
        selectedCard = null;
        List<EquipmentCard> cards = new ArrayList<>();
        if (menu.hasSelection()) {
            cards.add(new EquipmentCard(new ItemStack(Items.BARRIER), 0, 0, 75, 60,
                    Component.translatable("screen.zerocontact.equipment.unmount"), card -> {
                if (!menu.unMount()) return;
                if (selectedCard != null) selectedCard.setSelected(false);
                selectedCard = card;
                card.setSelected(true);
            }));
        }
        cards.addAll(menu.getCandidates().stream()
                .map(candidate -> new EquipmentCard(candidate.stack(), 0, 0, 75, 60,
                        candidate.stack().getHoverName(), card -> {
                    if (!menu.mountCandidate(candidate)) return;
                    if (selectedCard != null) selectedCard.setSelected(false);
                    selectedCard = card;
                    card.setSelected(true);
                }))
                .toList());
        equipmentList.setCards(cards);
        if (menu.getMountId().equals(displayedMountId) && menu.getTarget().equals(displayedTarget)) {
            equipmentList.setScrollAmount(scroll);
        }
        displayedMountId = menu.getMountId();
        displayedTarget = menu.getTarget();
        displayedRevision = menu.getCandidateRevision();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        refreshCards();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderAnchors(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, width, height, 0xe810141c);
        guiGraphics.fill(previewLeft, previewTop, previewRight, previewBottom, 0xff202936);
        guiGraphics.fill(listLeft, previewTop, width - MARGIN, previewBottom, 0xff18212c);
        guiGraphics.drawString(font, menu.hasSelection() ? Component.literal(menu.getMountId().getPath())
                : Component.translatable("screen.zerocontact.equipment.select_anchor"), listLeft + 6, previewTop + 6, 0xffffff);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.zerocontact.equipment.controls"),
                width / 2, height - 18, 0xffaab5c4);
        if (minecraft == null) return;
        if (minecraft.player == null) return;
        guiGraphics.enableScissor(previewLeft, previewTop, previewRight, previewBottom);
        try {
            float scale = Math.max(1, Math.min((previewRight - previewLeft) / 1.5f,
                    (previewBottom - previewTop - 16) / 2.1f)) * zoom;
            anchors = EquipmentPlayerPreview.render(guiGraphics, minecraft.player,
                    (previewLeft + previewRight) / 2, (previewTop + previewBottom) / 2, scale, yaw, pitch);
            if (anchors.isEmpty()) {
                guiGraphics.drawWordWrap(font, Component.translatable("screen.zerocontact.equipment.no_anchors"),
                        previewLeft + 8, previewTop + 8, Math.max(1, previewRight - previewLeft - 16), 0xffaab5c4);
            }
        } finally {
            guiGraphics.disableScissor();
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // This selector has no vanilla inventory slots or inventory label.
        guiGraphics.drawCenteredString(font, title, width / 2 - leftPos, 6 - topPos, 0xffffff);
    }

    private boolean inPreview(double x, double y) {
        return x >= previewLeft && x < previewRight && y >= previewTop && y < previewBottom;
    }

    private Anchor anchorAt(double x, double y) {
        if (!inPreview(x, y)) return null;
        return anchors.stream().filter(anchor -> inPreview(anchor.x(), anchor.y()))
                .filter(anchor -> Math.hypot(anchor.x() - x, anchor.y() - y) <= ANCHOR_RADIUS + 2)
                .min(Comparator.<Anchor>comparingDouble(anchor -> Math.hypot(anchor.x() - x, anchor.y() - y))
                        .thenComparing(Comparator.comparingDouble(Anchor::depth).reversed()))
                .orElse(null);
    }

    private void renderAnchors(GuiGraphics graphics, int mouseX, int mouseY) {
        Anchor hovered = anchorAt(mouseX, mouseY);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.enableScissor(previewLeft, previewTop, previewRight, previewBottom);
        for (Anchor anchor : anchors.stream().sorted(Comparator.comparingDouble(Anchor::depth)).toList()) {
            if (!inPreview(anchor.x(), anchor.y())) continue;
            boolean selected = anchor.target().equals(menu.getTarget()) && anchor.mount().mountId().equals(menu.getMountId());
            int color = anchor == hovered ? 0xffffffff : selected ? 0xff70e0aa : 0xff68b9ff;
            int x = Math.round(anchor.x()), y = Math.round(anchor.y());
            graphics.fill(x - 5, y - 5, x + 6, y + 6, 0xdd101820);
            graphics.renderOutline(x - 5, y - 5, 11, 11, color);
            graphics.fill(x - 1, y - 3, x + 2, y + 4, color);
            graphics.fill(x - 3, y - 1, x + 4, y + 2, color);
        }
        graphics.disableScissor();
        graphics.pose().popPose();
        if (hovered != null && minecraft != null && minecraft.player != null) {
            graphics.renderTooltip(font, hovered.target().resolve(minecraft.player).getHoverName().copy()
                    .append(" / " + hovered.mount().mountId().getPath()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inPreview(mouseX, mouseY)) {
            Anchor anchor = anchorAt(mouseX, mouseY);
            if (anchor != null) menu.selectAnchor(anchor.target(), anchor.mount().mountId());
            else draggingPreview = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingPreview) {
            yaw = (yaw + (float) dragX * 0.012f) % ((float) Math.PI * 2);
            pitch = Mth.clamp(pitch + (float) dragY * 0.012f, -1.05f, 1.05f);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingPreview) {
            draggingPreview = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (inPreview(mouseX, mouseY)) {
            zoom = Mth.clamp(zoom + (float) delta * 0.1f, 0.6f, 1.8f);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindingHandler.TOGGLE_MODULAR_MENU.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
