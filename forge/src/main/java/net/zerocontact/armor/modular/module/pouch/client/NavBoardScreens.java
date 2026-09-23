package net.zerocontact.armor.modular.module.pouch.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Local wearer's transient screen sessions. Uploads and composition run only at the render boundary.
 */
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NavBoardScreens {
    public static final int CHUNK_RADIUS = 8;
    public static final int MAP_SIZE = (CHUNK_RADIUS * 2 + 1) * 16;

    /**
     * Visible world width in blocks; height follows the screen aspect ratio.
     */
    public static int viewWidthBlocks() {
        return 256;
    }

    /**
     * Method access prevents other classes from retaining an inlined radius after hot reload.
     */
    public static int chunkRadius() {
        return CHUNK_RADIUS;
    }

    private record Key(EquipmentTarget target, ResourceLocation mount) {
    }

    private static final Map<Key, Session> SESSIONS = new HashMap<>();
    private static final Map<ItemStack, NavBoardScreenTarget> VISIBLE = new IdentityHashMap<>();
    private static ClientLevel currentLevel;

    private NavBoardScreens() {
    }

    private static final class Session {
        final ResourceLocation item;
        int[] pixels;
        int originX, originZ;
        boolean upload;
        NavBoardScreenTarget screen;

        Session(ResourceLocation item) {
            this.item = item;
        }

        void close() {
            if (screen != null) screen.close();
        }
    }

    public static void sampled(EquipmentTarget target, ResourceLocation mount, ResourceLocation item,
                               ChunkPos center, int radius, NavBoardContainer cap) {
        checkLevel();
        Key key = new Key(target, mount);
        Session session = SESSIONS.get(key);
        if (session == null || !session.item.equals(item)) {
            // Never leave the previous item's GPU resources attached to the new session.
            if (session != null) retire(session);
            session = new Session(item);
            SESSIONS.put(key, session);
        }
        int mapSize = (radius * 2 + 1) * 16;
        session.originX = (center.x - radius) * 16;
        session.originZ = (center.z - radius) * 16;
        session.pixels = new int[mapSize * mapSize];
        // A complete small snapshot also captures removed tiles and survives capability replacement.
        for (var entry : cap.getActiveTiles().entrySet()) {
            ChunkPos pos = new ChunkPos(entry.getKey());
            int dx = pos.getMinBlockX() - session.originX;
            int dz = pos.getMinBlockZ() - session.originZ;
            if (dx < 0 || dz < 0 || dx + 16 > mapSize || dz + 16 > mapSize) continue;
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    session.pixels[(dz + z) * mapSize + dx + x] = entry.getValue().getPixel(x, z);
                }
            }
        }
        session.upload = true;
    }

    private static void retire(Session session) {
        if (com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()) session.close();
        else com.mojang.blaze3d.systems.RenderSystem.recordRenderCall(session::close);
    }

    private static void checkLevel() {
        var level = Minecraft.getInstance().level;
        if (level != currentLevel) {
            clear();
            currentLevel = level;
        }
    }

    private static void clear() {
        SESSIONS.values().forEach(NavBoardScreens::retire);
        SESSIONS.clear();
        VISIBLE.clear();
        currentLevel = null;
    }

    public static NavBoardScreenTarget forStack(ItemStack stack) {
        return VISIBLE.get(stack);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        checkLevel();
        VISIBLE.clear();
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || mc.level == null || !player.isAlive() || player.isSpectator()) {
            clear();
            return;
        }
        Map<Key, ItemStack> active = new HashMap<>();
        ModuleQuery.streamMounted(player).forEach(ref -> ref.stack().getCapability(CapabilityRegistries.NAV_BOARD)
                .ifPresent(cap -> {
                    if (cap.isEnabled()) active.put(new Key(ref.equipmentTarget(), ref.mountId()), ref.stack());
                }));
        SESSIONS.entrySet().removeIf(entry -> {
            var stack = active.get(entry.getKey());
            if (stack == null || !entry.getValue().item.equals(
                    net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                entry.getValue().close();
                return true;
            }
            return false;
        });
        float partial = mc.isPaused() ? 1 : event.renderTickTime;
        double playerX = Mth.lerp(partial, player.xo, player.getX());
        double playerZ = Mth.lerp(partial, player.zo, player.getZ());
        float yaw = Mth.rotLerp(partial, player.yRotO, player.getYRot());
        for (var entry : SESSIONS.entrySet()) {
            Session session = entry.getValue();
            if (session.screen == null) {
                Key key = entry.getKey();
                // Stable names also avoid growing Minecraft's memoized RenderType cache on every toggle.
                session.screen = new NavBoardScreenTarget(new ResourceLocation(ZeroContact.MOD_ID,
                        "navboard/" + key.target().slot() + "/" + key.target().index() + "/"
                                + key.mount().getNamespace() + "/" + key.mount().getPath()));
            }
            if (session.upload) {
                session.screen.upload(session.pixels);
                session.upload = false;
            }
            session.screen.render(mc.level, playerX, playerZ,
                    session.originX, session.originZ, yaw, partial);
            VISIBLE.put(active.get(entry.getKey()), session.screen);
        }
    }
}
