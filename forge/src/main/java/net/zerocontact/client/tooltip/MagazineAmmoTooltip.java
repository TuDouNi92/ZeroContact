package net.zerocontact.client.tooltip;

import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.caliber.CaliberSerializer;
import net.zerocontact.compat.magazines.MagazineRoundStack;
import net.zerocontact.compat.magazines.MagazineRoundStack.Group;
import net.zerocontact.compat.magazines.MagazineRoundStack.Round;
import net.zerocontact.item.ammo.GenerateAmmo;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Read-only feed-order preview, shared across tooltip renderers recreated each frame. */
public final class MagazineAmmoTooltip {
    private static final int GAP = 8;
    private static final int INDENT = 6;
    private static final long SCROLL_MILLIS = 220;
    private static final Map<ItemStack, ScrollState> STATES = new WeakHashMap<>();

    private MagazineAmmoTooltip() {}

    private static List<Group> groups(ItemStack magazine) {
        if (magazine.isEmpty() || !(magazine.getItem() instanceof MagazineItem item)) return List.of();
        int count = Math.min(Math.max(0, item.getAmmoCount(magazine)),
                Math.max(0, MagazineItem.getMaxCapacity(magazine)));
        Round fallback = magazine.getTag() == null ? null
                : Round.load(magazine.getTag().getCompound(CaliberSerializer.AI_AMMO));
        if (fallback == null) {
            String family = MagazineItem.getMagazineFamilyId(magazine);
            ResourceLocation id = family == null || family.isBlank() ? null
                    : MagazineFamilySystem.getAmmoTypeForFamily(family);
            if (id == null) id = item.getAmmoId(magazine);
            if (id != null && !id.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                fallback = new Round(id.toString(), CaliberSerializer.DEFAULT_AMMO);
            }
        }
        MagazineRoundStack rounds = MagazineRoundStack.load(magazine.getTag(), count, fallback);
        return rounds == null ? List.of() : rounds.feedGroups(2);
    }

    private static ItemStack displayStack(Round round) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(round.variant()));
        return item instanceof GenerateAmmo ? item.getDefaultInstance()
                : AmmoItemBuilder.create().setId(new ResourceLocation(round.ammoId())).setCount(1).build();
    }

    public static ItemStack topAmmo(ItemStack magazine) {
        List<Group> groups = groups(magazine);
        return groups.isEmpty() ? ItemStack.EMPTY : displayStack(groups.get(0).round());
    }

    private static String line(List<Group> groups, int index) {
        String key = "tooltip.zerocontact.magazine." + (index == 0 ? "current" : "next");
        if (groups.size() <= index) {
            String empty = "tooltip.zerocontact.magazine." + (index == 0 ? "empty" : "none");
            return Component.translatable(key, Component.translatable(empty)).getString();
        }
        Group group = groups.get(index);
        Component ammo = displayStack(group.round()).getHoverName().copy().append(" x" + group.count());
        return Component.translatable(key, ammo).getString();
    }

    private static ScrollState state(ItemStack magazine) {
        List<Group> groups = groups(magazine);
        String current = line(groups, 0);
        String next = line(groups, 1);
        long now = Util.getMillis();
        ScrollState state = STATES.computeIfAbsent(magazine, ignored -> new ScrollState(current, next, now));
        // Reopening a tooltip should show the latest contents immediately.
        if (now - state.lastSeen > 750) {
            state.current = state.previousCurrent = current;
            state.next = state.previousNext = next;
        } else if (!current.equals(state.current) || !next.equals(state.next)) {
            state.previousCurrent = state.current;
            state.previousNext = state.next;
            state.current = current;
            state.next = next;
            state.changedAt = now;
        }
        if (now - state.changedAt >= SCROLL_MILLIS) {
            state.previousCurrent = state.current;
            state.previousNext = state.next;
        }
        state.lastSeen = now;
        return state;
    }

    public static int extraWidth(Font font, ItemStack magazine) {
        if (magazine.isEmpty() || !(magazine.getItem() instanceof MagazineItem)) return 0;
        ScrollState state = state(magazine);
        int current = Math.max(font.width(state.current), font.width(state.previousCurrent));
        int next = INDENT + Math.max(font.width(state.next), font.width(state.previousNext));
        return GAP + Math.max(current, next) + 4;
    }

    public static void render(Font font, GuiGraphics graphics, ItemStack magazine, int x, int y) {
        if (magazine.isEmpty() || !(magazine.getItem() instanceof MagazineItem)) return;
        ScrollState state = state(magazine);
        int height = font.lineHeight + 3;
        float progress = Math.min(1F, (Util.getMillis() - state.changedAt) / (float) SCROLL_MILLIS);
        // Smooth the roll without tying its speed to the frame rate.
        float offset = height * progress * progress * (3F - 2F * progress);
        int width = extraWidth(font, magazine) - GAP;
        drawRow(font, graphics, state.previousCurrent, state.current, x + GAP, y + 4,
                width, height, offset, 0x70e000);
        drawRow(font, graphics, state.previousNext, state.next, x + GAP + INDENT, y + 4 + height,
                width - INDENT, height, offset, 0x8d0801);
    }

    private static void drawRow(Font font, GuiGraphics graphics, String previous, String current,
                                int x, int y, int width, int height, float offset, int color) {
        graphics.enableScissor(x, y, x + width, y + height);
        graphics.pose().pushPose();
        try {
            if (previous.equals(current)) {
                graphics.drawString(font, current, x, y, color, false);
            } else {
                graphics.pose().translate(0, -offset, 0);
                graphics.drawString(font, previous, x, y, color, false);
                graphics.drawString(font, current, x, y + height, color, false);
            }
        } finally {
            graphics.pose().popPose();
            graphics.disableScissor();
        }
    }

    private static final class ScrollState {
        private String current;
        private String next;
        private String previousCurrent;
        private String previousNext;
        private long changedAt;
        private long lastSeen;

        private ScrollState(String current, String next, long now) {
            this.current = this.previousCurrent = current;
            this.next = this.previousNext = next;
            this.changedAt = this.lastSeen = now;
        }
    }
}
