package net.zerocontact.armor.modular.client.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.client.network.s2c.UpdateNavBoardPacket;
import net.zerocontact.armor.modular.module.pouch.container.navboard.ChunkSampler;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardChunkTile;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.armor.modular.module.pouch.client.NavBoardScreens;

public final class NavBoardUpdateHandler {

    private NavBoardUpdateHandler() {}

    public static void handle(UpdateNavBoardPacket packet) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var level = minecraft.level;
        if (player == null || level == null || !player.isAlive() || player.isSpectator()
                || !level.dimension().location().equals(packet.dimension())) return;
        var equipment = packet.target().resolve(player);
        if (equipment.isEmpty() || !packet.equipmentItem().equals(ForgeRegistries.ITEMS.getKey(equipment.getItem()))) return;
        equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).ifPresent(modules -> {
            var module = modules.getModule(packet.mountId());
            if (module.isEmpty() || !packet.moduleItem().equals(ForgeRegistries.ITEMS.getKey(module.getItem()))) return;
            module.getCapability(CapabilityRegistries.NAV_BOARD).ifPresent(cap -> {
                if (!cap.isEnabled()) {
                    cap.clearTiles();
                    return;
                }
                cap.prepareTiles(packet.dimension());
                ChunkPos center = player.chunkPosition();
                final int radius = NavBoardScreens.chunkRadius();
                cap.getActiveTiles().keySet().removeIf(key -> {
                    ChunkPos pos = new ChunkPos(key);
                    return Math.abs(pos.x - center.x) > radius || Math.abs(pos.z - center.z) > radius;
                });
                for (int x = center.x - radius; x <= center.x + radius; x++) {
                    for (int z = center.z - radius; z <= center.z + radius; z++) {
                        ChunkAccess chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                        long key = ChunkPos.asLong(x, z);
                        if (!(chunk instanceof LevelChunk loaded)) {
                            cap.getActiveTiles().remove(key);
                            continue;
                        }
                        var tile = cap.getActiveTiles().computeIfAbsent(key, ignored -> new NavBoardChunkTile());
                        for (int localZ = 0; localZ < 16; localZ++) {
                            for (int localX = 0; localX < 16; localX++) {
                                tile.setPixel(localX, localZ, ChunkSampler.colorForPos(level, loaded, localX, localZ));
                            }
                        }
                    }
                }
                NavBoardScreens.sampled(packet.target(), packet.mountId(), packet.moduleItem(), center, radius, cap);
            });
        });
    }
}
