package net.zerocontact.item.kit.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.zerocontact.item.kit.RepairSession;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RepairController {

    private final Map<UUID, RepairSession> sessions = new HashMap<>();
    private static RepairController repairController;

    public static RepairController get() {
        if (repairController == null) {
            repairController = new RepairController();
        }

        return repairController;
    }

    public Map<UUID, RepairSession> getSessions() {
        return sessions;
    }

    public @Nullable RepairSession getSession(ServerPlayer player) {
        return sessions.get(player.getUUID());
    }

    public boolean started(ServerPlayer player) {
        return this.getSessions().containsKey(player.getUUID());
    }

    public void start(ServerPlayer player, int targetSlot, @Nullable AbstractContainerMenu source) {
        sessions.put(
                player.getUUID(),
                new RepairSession(targetSlot, source)
        );
    }

    public void stop(ServerPlayer player) {
        sessions.remove(player.getUUID());
    }

}
