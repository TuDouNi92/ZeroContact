package net.zerocontact.armor.modular.module.nvg.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.registries.ModSoundEventsReg;
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
        private final NvgExposureState exposure = new NvgExposureState();
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
            }
            var previousPhase = state.phase();
            state.tick(capability.outOfPower());
            var type = ((INvg) module.getItem()).getNVGType();
            if (type == INvg.Type.GREEN || type == INvg.Type.WHITE) {
                BlockPos eyePos = BlockPos.containing(minecraft.player.getEyePosition());
                int blockLight = minecraft.level.getBrightness(LightLayer.BLOCK, eyePos);
                var dimension = minecraft.level.dimensionType();
                boolean exposedToSky = dimension.hasSkyLight() && minecraft.level.canSeeSky(eyePos);
                // Use the live client sky factor, as LightTexture does. The no-argument
                // getSkyDarken() reads a cached integer; raw sky light can be 15 even at night.
                exposure.tick(blockLight, exposedToSky, minecraft.level.getSkyDarken(1.0F));
            }
            // Set a stable pose even if the view model was not rendered during the transition.
            if (previousPhase != state.phase()) {
                switch (state.phase()) {
                    case ON -> {
                        minecraft.player.playNotifySound(ModSoundEventsReg.NVG_BUZZ, SoundSource.PLAYERS, 1, 1);
                        minecraft.player.playNotifySound(ModSoundEventsReg.NVG_CLICK, SoundSource.PLAYERS, 1, 1);
                    }
                    case OFF -> {
                        minecraft.player.playNotifySound(ModSoundEventsReg.NVG_BUZZ, SoundSource.PLAYERS, 1, 0.7f);
                        minecraft.player.playNotifySound(ModSoundEventsReg.NVG_CLICK, SoundSource.PLAYERS, 1, 0.7f);
                    }
                }


            }
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

        public float gain(float partialTick) {
            return exposure.gain(partialTick);
        }

        public void reset() {
            ++generation;
            rendering = false;
            module = ItemStack.EMPTY;
            identity = null;
            state.reset();
            exposure.reset();
        }
    }
}
