package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.model.MountDefinition;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Scoped to the GUI player render; never captures world or item-card renders. */
public final class EquipmentAnchorCapture implements AutoCloseable {
    private static EquipmentAnchorCapture active;
    private final Map<ItemStack, EquipmentTarget> targets = new IdentityHashMap<>();
    private final Map<Key, Anchor> anchors = new LinkedHashMap<>();

    private record Key(EquipmentTarget target, net.minecraft.resources.ResourceLocation mountId) {}
    public record Anchor(EquipmentTarget target, MountDefinition mount, float x, float y, float depth) {}

    public EquipmentAnchorCapture(Player player) {
        EquipmentTarget.wornBy(player).forEach((target, stack) -> {
            if (!stack.isEmpty()) targets.put(stack, target);
        });
        if (active != null) throw new IllegalStateException("Nested equipment preview capture");
        active = this;
    }

    public static void capture(ItemStack stack, MountDefinition mount, GeoBone bone, PoseStack pose) {
        if (active == null || bone.isHidden()) return;
        EquipmentTarget target = active.targets.get(stack);
        if (target == null) return;
        // GeoRenderLayer runs after prepMatrixForBone, including all parent/player/GUI transforms.
        Vector3f point = pose.last().pose().transformPosition(
                new Vector3f(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f));
        if (!point.isFinite()) return;
        active.anchors.put(new Key(target, mount.mountId()),
                new Anchor(target, mount, point.x, point.y, point.z));
    }

    public List<Anchor> anchors() {
        return new ArrayList<>(anchors.values());
    }

    @Override
    public void close() {
        if (active == this) active = null;
    }
}
