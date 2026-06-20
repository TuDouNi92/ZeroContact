package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.menu.AmmoSelectorMenu;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;

public class AmmoSelectorScreen extends AbstractContainerScreen<AmmoSelectorMenu> {
    private final Minecraft mc = Minecraft.getInstance();
    private final AmmoSelectorRenderUtil.Ring ring = new AmmoSelectorRenderUtil.Ring(
            (float) mc.getWindow().getGuiScaledWidth() / 2,
            (float) mc.getWindow().getGuiScaledHeight() / 2,
            24,
            96,
            menu.ammo.size(),
            4f
    );
    private boolean lastHovered;

    public AmmoSelectorScreen(AmmoSelectorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSelector(guiGraphics, mouseX, mouseY);
        playSelectedSound(mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

    void playSelectedSound(int mouseX, int mouseY) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        if (hovered && !lastHovered) {
            ClientLevel level = mc.level;
            Player player = mc.player;
            if (level == null || player == null) return;
            level.playSound(player, player.blockPosition(), ModSoundEventsReg.GUI_SELECTOR, SoundSource.PLAYERS);
        }
        lastHovered = hovered;
    }

    public void renderSelector(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (mc.player == null) return;
        if (mc.isPaused()) return;
        AmmoSelectorRenderUtil.drawSegmentedRing(
                guiGraphics,
                ring,
                32,
                0x55000000
        );
        renderAmmoLabel(ring, guiGraphics);
        renderHighLight(guiGraphics, mouseX, mouseY, ring);
    }

    private static void renderHighLight(GuiGraphics guiGraphics, int mouseX, int mouseY, AmmoSelectorRenderUtil.Ring ring) {
        int index = AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY, ring);
        if (AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY, ring) != -1) {
            AmmoSelectorRenderUtil.drawSegmentedRingPart(guiGraphics, ring, index, 32, 0x33FFFFFF);
        }
    }

    public void renderAmmoLabel(AmmoSelectorRenderUtil.Ring ring, GuiGraphics guiGraphics) {
        float centerRadius = (ring.outerRadius() + ring.innerRadius()) / 2;
        for (int i = 0; i < ring.segmentCount(); i++) {
            double sectionAngle = 2.0 * Math.PI / ring.segmentCount();
            double baseAngle = -Math.PI / 2.0;
            double angle = baseAngle + sectionAngle * i + sectionAngle / 2.0;
            int x = (int) Math.floor(ring.centerX() + Math.cos(angle) * centerRadius);
            int y = (int) Math.floor(ring.centerY() + Math.sin(angle) * centerRadius);
            ItemStack stack = menu.ammo.get(i).getKey();
            Integer count = menu.ammo.get(i).getValue();
            guiGraphics.renderFakeItem(stack, x - 8, y - 8);
            guiGraphics.drawString(font, count.toString(), x - font.width(count.toString()) / 2, y + 16, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int ammoIndex = AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY, ring);
            if (ammoIndex == -1) return true;
            ItemStack ammoItem = menu.ammo.get(ammoIndex).getKey();
            ModMessages.sendToServer(new NetworkHandler.SelectAmmoPacket(ammoItem));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (KeyBindingHandler.TOGGLE_AMMO_SELECTOR.matches(keyCode, scanCode)) {
            onClose();
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int hoveredIndex = AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY, ring);
        return hoveredIndex != -1;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }
}
