package net.zerocontact.armor.modular.module.nvg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.capability.CapabilityRegistries;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.Map;
import java.util.Objects;

/**
 * Local presentation only. The server capability remains authoritative.
 */
public final class ClientInteractionManger {
    private static final ClientEffect EFFECT = new ClientEffect();

    private ClientInteractionManger() {
    }

    public static ClientEffect get() {
        return EFFECT;
    }

    public static final class ClientEffect {
        private final NvgEffectState state = new NvgEffectState();
        private ItemStack module = ItemStack.EMPTY;
        private ModuleIdentity identity;
        private Object level;
        private Object player;
        private long generation;
        private boolean rendering;

        private ClientEffect() {
        }

        private record ModuleIdentity(ResourceLocation mount, Item item, long animationId) {
        }

        public void tick(Minecraft minecraft) {
            if (minecraft.level != level || minecraft.player != player) {
                reset();
                level = minecraft.level;
                player = minecraft.player;
            }
            if (minecraft.player == null || minecraft.level == null
                    || !minecraft.player.isAlive() || minecraft.player.isSpectator()) {
                reset();
                return;
            }
            var selected = minecraft.player.getItemBySlot(EquipmentSlot.HEAD)
                    .getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                    .map(cap -> cap)
                    .flatMap(container -> container.getMountedModules()
                            .entrySet()
                            .stream()
                            .filter(
                                    entry -> entry.getValue().getItem() instanceof INvg
                                            && entry.getValue().getItem() instanceof GeoItem
                            )
                            .min(Map.Entry.comparingByKey()))
                    .orElse(null);
            if (selected == null) {
                reset();
                return;
            }
            ItemStack selectedStack = selected.getValue();
            // getId cannot distinguish a missing numeric tag from ID zero in GeckoLib 4.7.
            if (selectedStack.getTag() == null
                    || !selectedStack.getTag().contains(GeoItem.ID_NBT_KEY, Tag.TAG_ANY_NUMERIC)) {
                reset(); // Wait for the server to assign and sync this mounted stack's ID.
                return;
            }
            var next = new ModuleIdentity(selected.getKey(), selectedStack.getItem(), GeoItem.getId(selectedStack));
            boolean changedModule = !Objects.equals(identity, next);
            if (changedModule) reset();
            identity = next;
            module = selectedStack; // Sync rebuilds stacks but preserves their identity.
            var capability = module.getCapability(CapabilityRegistries.NVG).map(cap -> cap).orElse(null);
            if (capability == null) {
                reset();
                return;
            }
            boolean desired = capability.getEnabled() && !capability.outOfPower();
            if (changedModule || desired != state.desiredEnabled()) {
                if (changedModule && !desired) state.reset();
                else state.request(desired);
                trigger(minecraft, changedModule && !desired ? "off_pose" : desired ? "activate" : "deactivate");
            }
            var previousPhase = state.phase();
            state.tick(capability.outOfPower());
            // Set a stable pose even if the view model was not rendered during the transition.
            if (previousPhase != state.phase()) {
                trigger(minecraft, state.phase() == NvgEffectState.Phase.ON ? "on_pose" : "off_pose");
            }
        }

        private void trigger(Minecraft minecraft, String animation) {
            GeoItem animatable = (GeoItem) module.getItem();
            long id = identity.animationId();
            long transition = ++generation;
            var controller = animatable.getAnimatableInstanceCache().getManagerForId(id)
                    .getAnimationControllers().get("controller");
            if (controller == null) return;
            // Bind before triggering; callbacks from previews and old transitions cannot change effects.
            controller.setCustomInstructionKeyframeHandler(event -> {
                if (rendering && generation == transition)
                    state.marker(event.getKeyframeData().getInstructions());
            });
            animatable.stopTriggeredAnim(minecraft.player, id, "controller", null);
            animatable.triggerAnim(minecraft.player, id, "controller", animation);
        }

        public ItemStack module() {
            return module;
        }

        public void beginRender() {
            rendering = true;
        }

        public void endRender() {
            rendering = false;
        }

        public NvgEffectState.Phase phase() {
            return state.phase();
        }

        public boolean wantsShader() {
            return state.wantsShader();
        }

        public float vignetteAlpha(float partialTick) {
            return state.alpha(partialTick);
        }

        public void reset() {
            ++generation;
            rendering = false;
            module = ItemStack.EMPTY;
            identity = null;
            state.reset();
        }
    }
}
