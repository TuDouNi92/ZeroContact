package net.zerocontact.item.helmet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.Toggleable;
import net.zerocontact.animation_data.AnimateData;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.zerocontact.ZeroContact.MOD_ID;

public class AltynVisor {
    public static class WithVisor extends BaseGeoHelmet implements Toggleable {
        private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_altyn_visor_olive.png");
        private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/helmet/helmet_altyn_enabled_visor_olive.geo.json");
        private static final ResourceLocation animation = new ResourceLocation(MOD_ID, "animations/visor_altyn_switch.animation.json");
        private static final ResourceLocation VISOR_TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/altyn_vision_sky_240p.png");
        private static final RawAnimation ENABLE_VISOR = RawAnimation.begin().then("switch_disabled_to_enabled", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation DISABLE_VISOR = RawAnimation.begin().then("switch_enabled_to_disabled", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        public WithVisor(int absorb, int defaultDurability, float bluntReduction, float penetrateReduction) {
            super(absorb, defaultDurability, texture, model, animation, bluntReduction, penetrateReduction);
            SingletonGeoAnimatable.registerSyncedAnimatable(this);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
            controllerRegistrar.add(new AnimationController<>(this, "controller", 0, state -> {
                        ItemStack animStack = state.getData(DataTickets.ITEMSTACK);
                        AnimationProcessor.QueuedAnimation queuedAnimation = state.getController().getCurrentAnimation();
                        if (animStack.getItem() instanceof Toggleable toggleable) {
                            if (queuedAnimation != null) {
                                toggleable.saveAnimData(AnimateData.VisorAnimateData.create(
                                        queuedAnimation.animation().name(),
                                        queuedAnimation.animation().length(),
                                        state.getController().isPlayingTriggeredAnimation()
                                ), animStack);
                            }

                        }
                        return PlayState.CONTINUE;
                    })
                            .receiveTriggeredAnimations()
                            .triggerableAnim("enable", ENABLE_VISOR)
                            .triggerableAnim("disable", DISABLE_VISOR)
            );
        }

        @Override
        public ResourceLocation getVisorTexture() {
            return VISOR_TEXTURE;
        }

        @Override
        public AnimateData.VisorAnimateData readAnimData(ItemStack stack) {
            CompoundTag tag = stack.getOrCreateTag().getCompound("AnimCompound");
            AnimateData.VisorAnimateData animateData = AnimateData.VisorAnimateData.create("empty", 0, false);
            String animName = tag.getString("anim_name");
            double length = tag.getDouble("anim_length");
            boolean isPlaying = tag.getBoolean("anim_playing");
            animateData.set(animName, length, isPlaying);
            return animateData;
        }

        @Override
        public void saveAnimData(@Nullable AnimateData.VisorAnimateData animateData, ItemStack stack) {
            if (animateData == null) return;
            CompoundTag tag = stack.getOrCreateTag().getCompound("AnimCompound");
            tag.putString("anim_name", animateData.animationName);
            tag.putDouble("anim_length", animateData.animLength);
            tag.putBoolean("anim_playing", animateData.isPlaying);
            stack.getOrCreateTag().put("AnimCompound", tag);
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
        static class VisorAnim {
            @SubscribeEvent
            public static void visorTick(net.minecraftforge.event.TickEvent.PlayerTickEvent tickEvent) {
                Player player = tickEvent.player;
                if (player.level() instanceof ServerLevel serverLevel) {
                    ItemStack helmetStack = tickEvent.player.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmetStack.getItem() instanceof Toggleable toggleable
                            && helmetStack.getItem() instanceof SingletonGeoAnimatable animatable) {
                        if (toggleable.getTriggered(helmetStack)) {
                            if (toggleable.readStatus(helmetStack, "VisorOn")) {
                                animatable.triggerArmorAnim(player, GeoItem.getOrAssignId(helmetStack, serverLevel), "controller", "disable");
                            } else {
                                animatable.triggerArmorAnim(player, GeoItem.getOrAssignId(helmetStack, serverLevel), "controller", "enable");
                            }
                        }
                        toggleable.triggered(helmetStack, false);
                    }
                }
            }
        }

    }
}
