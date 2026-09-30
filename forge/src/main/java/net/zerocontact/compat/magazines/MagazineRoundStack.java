package net.zerocontact.compat.magazines;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.caliber.CaliberSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Bottom first, feed lips last. One entry represents one cartridge, not one projectile. */
public final class MagazineRoundStack {
    public static final String TAG = "ZeroContact_Rounds";

    public record Round(String ammoId, String variant) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(CaliberSerializer.AI_AMMO_ID, ammoId);
            tag.putString(CaliberSerializer.VARIANT, variant);
            return tag;
        }

        public static @Nullable Round load(@Nullable CompoundTag tag) {
            if (tag == null) return null;
            String ammoId = tag.getString(CaliberSerializer.AI_AMMO_ID);
            String variant = tag.getString(CaliberSerializer.VARIANT);
            if (ammoId.isBlank() || variant.isBlank()
                    || ResourceLocation.tryParse(ammoId) == null || ResourceLocation.tryParse(variant) == null) {
                return null;
            }
            return new Round(ammoId, variant);
        }
    }

    private final List<Round> rounds = new ArrayList<>();

    /** Null means the requested count cannot be recovered without inventing cartridges. */
    public static @Nullable MagazineRoundStack load(@Nullable CompoundTag tag, int count, @Nullable Round legacyRound) {
        MagazineRoundStack stack = new MagazineRoundStack();
        if (tag != null && tag.contains(TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size() && stack.size() < count; i++) {
                Round round = Round.load(list.getCompound(i));
                // Keep the position of an invalid/removed variant using the legacy fallback.
                if (round == null && legacyRound == null) return null;
                stack.push(round == null ? legacyRound : round);
            }
        }
        if (stack.size() < Math.max(0, count) && legacyRound == null) return null;
        while (stack.size() < Math.max(0, count)) stack.push(legacyRound);
        return stack;
    }

    public ListTag save() {
        ListTag list = new ListTag();
        for (Round round : rounds) list.add(round.save());
        return list;
    }

    public int size() {
        return rounds.size();
    }

    public void push(Round round) {
        rounds.add(round);
    }

    public Round peek() {
        return rounds.isEmpty() ? null : rounds.get(rounds.size() - 1);
    }

    public @Nullable Round pop() {
        return rounds.isEmpty() ? null : rounds.remove(rounds.size() - 1);
    }

    public record Group(Round round, int count) {}

    /** Consecutive cartridge runs in firing order; separated runs stay separate. */
    public List<Group> feedGroups(int limit) {
        List<Group> groups = new ArrayList<>();
        int index = rounds.size() - 1;
        while (index >= 0 && groups.size() < limit) {
            Round round = rounds.get(index);
            int count = 0;
            do {
                count++;
                index--;
            } while (index >= 0 && round.equals(rounds.get(index)));
            groups.add(new Group(round, count));
        }
        return List.copyOf(groups);
    }

    public MagazineRoundStack copy() {
        MagazineRoundStack copy = new MagazineRoundStack();
        copy.rounds.addAll(rounds);
        return copy;
    }
}
