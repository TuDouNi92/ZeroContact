package net.zerocontact.armor.modular.client.renderer;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.capability.CapabilityRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.ContextAwareAnimatableManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.armor.modular.model.EquipmentTarget;

import java.util.Comparator;

/** Camera-relative terminal model. Uses the actual mounted stack so the live screen stays attached. */
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FirstPersonNavBoardRenderer {
    // Camera coordinates: +X right, +Y up, -Z forward. Adjust these together for model fitting.
    private static final double OFFSET_X = 0.0;
    private static final double OFFSET_Y = -0.24;
    private static final double OFFSET_Z = -0.1;
    private static final float ROTATION_X = 75.0F;
    private static final float ROTATION_Y = 0.0F;
    private static final float ROTATION_Z = 0.0F;
    private static final float SCALE = 2F;
    private static boolean renderedThisFrame;
    private static VisibleModule visibleModule;
    private static Object renderedLevel;
    private static Object renderedPlayer;

    private record VisibleModule(EquipmentTarget target, ResourceLocation mount, Item item, long animationId) {
        static VisibleModule of(ModuleQuery.MountedModuleRef ref) {
            return new VisibleModule(ref.equipmentTarget(), ref.mountId(), ref.stack().getItem(), GeoItem.getId(ref.stack()));
        }
    }

    private FirstPersonNavBoardRenderer() {}

    @SubscribeEvent
    public static void beginFrame(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        renderedThisFrame = false;
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != renderedLevel || minecraft.player != renderedPlayer
                || minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player || minecraft.options.hideGui) {
            visibleModule = null;
        }
        renderedLevel = minecraft.level;
        renderedPlayer = minecraft.player;
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void render(RenderHandEvent event) {
        var minecraft = Minecraft.getInstance();
        if (renderedThisFrame || minecraft.player == null || minecraft.level == null
                || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player
                || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.options.hideGui) return;

        // Prefer an enabled terminal, then the one already on screen while it closes.
        var selected = ModuleQuery.streamMounted(minecraft.player)
                .filter(ref -> ref.stack().getItem() instanceof GeoItem
                        && ref.stack().getCapability(CapabilityRegistries.NAV_BOARD).isPresent()
                        && ref.stack().getTag() != null
                        && ref.stack().getTag().contains(GeoItem.ID_NBT_KEY, Tag.TAG_ANY_NUMERIC))
                .min(Comparator.comparingInt((ModuleQuery.MountedModuleRef ref) ->
                                ref.stack().getCapability(CapabilityRegistries.NAV_BOARD)
                                        .map(cap -> cap.isEnabled() ? 0 : VisibleModule.of(ref).equals(visibleModule) ? 1 : 2).orElse(2))
                        .thenComparing(ref -> ref.equipmentTarget().slot())
                        .thenComparingInt(ref -> ref.equipmentTarget().index())
                        .thenComparing(ModuleQuery.MountedModuleRef::mountId))
                .orElse(null);
        if (selected == null) {
            visibleModule = null;
            return;
        }
        var identity = VisibleModule.of(selected);
        boolean enabled = selected.stack().getCapability(CapabilityRegistries.NAV_BOARD)
                .map(NavBoardContainer::isEnabled).orElse(false);
        if (!enabled && !identity.equals(visibleModule)) {
            visibleModule = null;
            return; // Initially closed or replaced/unmounted terminals must not appear.
        }
        visibleModule = identity;
        renderedThisFrame = true;

        var pose = event.getPoseStack();
        pose.pushPose();
        try {
            // This event precedes held-item swing/equip transforms. Keep vanilla hands and NVG intact.
            pose.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
            pose.mulPose(Axis.XP.rotationDegrees(ROTATION_X));
            pose.mulPose(Axis.YP.rotationDegrees(ROTATION_Y));
            pose.mulPose(Axis.ZP.rotationDegrees(ROTATION_Z));
            pose.scale(SCALE, SCALE, SCALE);
            // Compensate GeoItemRenderer's item-space origin, as in FirstPersonNvgRenderer.
            pose.translate(-0.5, -0.51, -0.5);
            var module = selected.stack();
            IClientItemExtensions.of(module.getItem()).getCustomRenderer().renderByItem(
                    module, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, pose,
                    event.getMultiBufferSource(), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            // Inspect after rendering: GeckoLib has now advanced this perspective's animation.
            if (!enabled && closeFinished(module)) visibleModule = null;
        } finally {
            pose.popPose();
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean closeFinished(ItemStack module) {
        AnimatableManager<?> manager = ((GeoItem) module.getItem()).getAnimatableInstanceCache()
                .getManagerForId(GeoItem.getId(module));
        if (manager instanceof ContextAwareAnimatableManager<?, ?> contexts) {
            // GeoItem's perspective-aware cache is keyed by ItemDisplayContext.
            manager = ((ContextAwareAnimatableManager<?, ItemDisplayContext>) contexts)
                    .getManagerForContext(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        }
        var controller = manager.getAnimationControllers().get("navboard");
        if (controller == null || controller.getAnimationState() == AnimationController.State.STOPPED) return true;
        var current = controller.getCurrentAnimation();
        // HOLD_ON_LAST_FRAME enters PAUSED, not STOPPED; hasAnimationFinished alone misses it.
        return current != null && "close".equals(current.animation().name())
                && controller.getAnimationState() == AnimationController.State.PAUSED;
    }
}
