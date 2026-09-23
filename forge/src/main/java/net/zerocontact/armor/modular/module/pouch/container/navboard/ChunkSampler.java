package net.zerocontact.armor.modular.module.pouch.container.navboard;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;

public class ChunkSampler {
    public static int colorForPos(Level level, LevelChunk chunk, int localX, int localZ) {
        int worldX = chunk.getPos().getMinBlockX() + localX;
        int worldZ = chunk.getPos().getMinBlockZ() + localZ;
        int currentY = chunk.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                worldX,
                worldZ
        );

        if (currentY < level.getMinBuildHeight()) return 0;
        // Never load a neighboring chunk just to calculate shading.

        double shade = getShade(level, chunk, localX, localZ, currentY, worldX, worldZ);
        float factor = 0.85F + (float) shade * 0.25F;
        factor = Mth.clamp(factor, 0.65F, 1.15F);

        BlockPos blockPos = new BlockPos(worldX, currentY, worldZ);
        BlockState surfaceState = chunk.getBlockState(blockPos);
        MapColor mapColor = surfaceState.getMapColor(level, blockPos);

        int mapRgb = mapColor.calculateRGBColor(MapColor.Brightness.NORMAL);
        int r = (mapRgb >> 16) & 0xff;
        int g = (mapRgb >> 8) & 0xff;
        int b = mapRgb & 0xff;
        r = Mth.clamp((int) (r * factor), 0, 255);
        g = Mth.clamp((int) (g * factor), 0, 255);
        b = Mth.clamp((int) (b * factor), 0, 255);
        return 0xff000000 | (r << 16) | (g << 8) | b;

    }

    public static double getShade(
            Level level,
            ChunkAccess chunk,
            int localX,
            int localZ,
            int currentY,
            int worldX,
            int worldZ
    ) {
        var leftChunk = localX > 0
                ? chunk
                : level.getChunkSource().getChunk(
                chunk.getPos().x - 1,
                chunk.getPos().z,
                ChunkStatus.FULL,
                false
        );

        var rightChunk = localX < 15
                ? chunk
                : level.getChunkSource().getChunk(
                chunk.getPos().x + 1,
                chunk.getPos().z,
                ChunkStatus.FULL,
                false
        );

        var northChunk = localZ > 0
                ? chunk
                : level.getChunkSource().getChunk(
                chunk.getPos().x,
                chunk.getPos().z - 1,
                ChunkStatus.FULL,
                false
        );

        var southChunk = localZ < 15
                ? chunk
                : level.getChunkSource().getChunk(
                chunk.getPos().x,
                chunk.getPos().z + 1,
                ChunkStatus.FULL,
                false
        );

        int leftY = leftChunk == null
                ? currentY
                : leftChunk.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                worldX - 1,
                worldZ
        );

        int rightY = rightChunk == null
                ? currentY
                : rightChunk.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                worldX + 1,
                worldZ
        );

        int northY = northChunk == null
                ? currentY
                : northChunk.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                worldX,
                worldZ - 1
        );

        int southY = southChunk == null
                ? currentY
                : southChunk.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                worldX,
                worldZ + 1
        );

        float dx = (rightY - leftY) * 0.5F;
        float dz = (southY - northY) * 0.5F;

        Vec3 normal = new Vec3(-dx, 1.0, -dz).normalize();
        Vec3 light = new Vec3(-0.6, 1.0, -0.8).normalize();
        Vec3 flatNormal = new Vec3(0, 1, 0);
        double flatShade = flatNormal.dot(light);

        return normal.dot(light) - flatShade;
    }

}
