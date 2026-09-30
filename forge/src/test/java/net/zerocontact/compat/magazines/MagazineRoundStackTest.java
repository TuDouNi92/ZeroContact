package net.zerocontact.compat.magazines;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.zerocontact.compat.magazines.MagazineRoundStack.Round;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

/** Standalone NBT/persistence regressions; no game world or optional magazine classes required. */
public final class MagazineRoundStackTest {
    private static final Round FMJ = new Round("tacz:556x45", "zerocontact:556_fmj");
    private static final Round AP = new Round("tacz:556x45", "zerocontact:556_ap");
    private static final Round TRACER = new Round("tacz:556x45", "zerocontact:556_tracer");
    private static int checks;

    public static void main(String[] args) throws Exception {
        MagazineRoundStack stack = new MagazineRoundStack();
        stack.push(FMJ);
        stack.push(AP);
        stack.push(TRACER);
        check(TRACER.equals(stack.peek()) && stack.size() == 3, "peek does not consume");

        CompoundTag tag = new CompoundTag();
        tag.put(MagazineRoundStack.TAG, stack.save());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, bytes);
        CompoundTag persisted = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        MagazineRoundStack restored = MagazineRoundStack.load(persisted, 3, FMJ);
        check(TRACER.equals(restored.pop()), "disk NBT preserves top");
        check(AP.equals(restored.pop()) && FMJ.equals(restored.pop()), "disk NBT preserves mixed order");
        check(restored.pop() == null && restored.peek() == null, "empty stack has no phantom cartridge");

        MagazineRoundStack mixed = new MagazineRoundStack();
        mixed.push(FMJ);
        for (int i = 0; i < 18; i++) mixed.push(AP);
        for (int i = 0; i < 6; i++) mixed.push(FMJ);
        check(mixed.feedGroups(2).equals(java.util.List.of(
                new MagazineRoundStack.Group(FMJ, 6), new MagazineRoundStack.Group(AP, 18))),
                "preview counts consecutive runs from the feed lips, not total variant counts");
        check(mixed.size() == 25 && FMJ.equals(mixed.peek()), "preview does not consume cartridges");
        mixed.pop();
        check(mixed.feedGroups(2).get(0).count() == 5, "current run counts down after firing");
        for (int i = 0; i < 5; i++) mixed.pop();
        check(mixed.feedGroups(2).equals(java.util.List.of(
                new MagazineRoundStack.Group(AP, 18), new MagazineRoundStack.Group(FMJ, 1))),
                "next run becomes current when the top run is exhausted");
        check(mixed.feedGroups(0).isEmpty() && new MagazineRoundStack().feedGroups(2).isEmpty(),
                "zero limit and empty magazine have no preview runs");
        check(MagazineRoundStack.load(null, 30, AP).feedGroups(2).equals(java.util.List.of(
                new MagazineRoundStack.Group(AP, 30))), "legacy magazine has one run and no next run");

        MagazineRoundStack copy = stack.copy();
        copy.pop();
        check(stack.size() == 3 && copy.size() == 2, "unload snapshot is independent");
        MagazineRoundStack reduced = MagazineRoundStack.load(persisted, 2, FMJ);
        check(AP.equals(reduced.pop()) && FMJ.equals(reduced.pop()), "count reconciliation removes the feed-lip end");
        MagazineRoundStack legacy = MagazineRoundStack.load(null, 30, AP);
        check(legacy.size() == 30, "legacy magazine count migrates");
        while (legacy.size() > 0) check(AP.equals(legacy.pop()), "legacy variant migrates on every round");
        check(legacy.feedGroups(2).isEmpty(), "exhausted legacy magazine has no preview runs");
        check(MagazineRoundStack.load(persisted, 0, FMJ).size() == 0, "zero count clears stale list");
        check(Round.load(null) == null, "missing round tag has no cartridge");
        MagazineRoundStack withoutLegacy = MagazineRoundStack.load(persisted, 3, null);
        check(withoutLegacy != null && TRACER.equals(withoutLegacy.pop())
                && AP.equals(withoutLegacy.pop()) && FMJ.equals(withoutLegacy.pop()),
                "complete persisted stack survives missing legacy ammo metadata");
        MagazineRoundStack emptyWithoutLegacy = MagazineRoundStack.load(null, 0, null);
        check(emptyWithoutLegacy != null && emptyWithoutLegacy.size() == 0,
                "empty magazine needs no legacy fallback");
        check(MagazineRoundStack.load(null, 1, null) == null,
                "missing legacy cartridge is unresolved rather than a null stack entry");
        check(MagazineRoundStack.load(persisted, 4, null) == null,
                "missing entries without a fallback do not invent cartridges");

        ListTag damaged = new ListTag();
        damaged.add(AP.save());
        damaged.add(new CompoundTag());
        damaged.add(TRACER.save());
        CompoundTag damagedTag = new CompoundTag();
        damagedTag.put(MagazineRoundStack.TAG, damaged);
        CompoundTag beforeRecovery = damagedTag.copy();
        check(MagazineRoundStack.load(damagedTag, 3, null) == null,
                "damaged entry without a fallback reports failed recovery");
        check(beforeRecovery.equals(damagedTag), "failed recovery preserves original NBT");
        MagazineRoundStack repaired = MagazineRoundStack.load(damagedTag, 4, FMJ);
        check(FMJ.equals(repaired.pop()), "missing entry recovers using the legacy round");
        check(TRACER.equals(repaired.pop()) && FMJ.equals(repaired.pop()) && AP.equals(repaired.pop()),
                "invalid entry keeps its position rather than shifting the remaining rounds");

        // Repeated partial use, save/load, then topping off must never reverse surviving cartridges.
        Random random = new Random(20260930);
        for (int cycle = 0; cycle < 100; cycle++) {
            Round[] expected = new Round[30];
            MagazineRoundStack magazine = new MagazineRoundStack();
            for (int i = 0; i < expected.length; i++) {
                expected[i] = random.nextBoolean() ? AP : FMJ;
                magazine.push(expected[i]);
            }
            int spent = random.nextInt(30);
            for (int i = 29; i >= 30 - spent; i--) check(expected[i].equals(magazine.pop()), "shot order");
            CompoundTag partial = new CompoundTag();
            partial.put(MagazineRoundStack.TAG, magazine.save());
            magazine = MagazineRoundStack.load(partial, 30 - spent, FMJ);
            magazine.push(TRACER);
            check(TRACER.equals(magazine.pop()), "topped-off cartridge fires first");
            for (int i = 29 - spent; i >= 0; i--) check(expected[i].equals(magazine.pop()), "partial reload order");
        }
        System.out.println("MagazineRoundStack: " + checks + " checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
