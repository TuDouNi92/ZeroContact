package net.zerocontact.entity.ai;

import net.zerocontact.entity.ArmedRaider;

import java.util.ArrayList;
import java.util.List;

public class MTeam {
    private static final List<ArmedRaider> buffer = new ArrayList<>();
    private static int factionCounter = 0;

    public static void registerEntity(ArmedRaider entity) {
        buffer.add(entity);
        if (buffer.size() == 3) {
            String newFactionId = "faction_" + (factionCounter++);
            for (ArmedRaider mob : buffer) {
                mob.setFactionId(newFactionId);
            }
            buffer.clear();
        }
    }
}
