package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.Toggleable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import static net.zerocontact.ZeroContact.MOD_ID;

public class AltynVisor {
    public static class WithVisor extends BaseGeoHelmet implements Toggleable {
        private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_altyn_visor_olive.png");
        private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/helmet/helmet_altyn_enabled_visor_olive.geo.json");
        private static final ResourceLocation animation = new ResourceLocation(MOD_ID, "animations/visor_altyn_switch.animation.json");
        private static final ResourceLocation visor = new ResourceLocation(MOD_ID, "textures/gui/altyn_vision.tga");
        private static final RawAnimation Enable = RawAnimation.begin().then("switch_disabled_to_enabled", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation DISABLE = RawAnimation.begin().then("switch_enabled_to_disabled", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
        private boolean isEnable = false;
        private boolean isToggling = false;

        public WithVisor(int absorb, int defaultDurability) {
            super(absorb, defaultDurability, texture, model, animation);
            SingletonGeoAnimatable.registerSyncedAnimatable(this);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
            controllerRegistrar.add(new AnimationController<>(this, "controller", 10, state -> PlayState.STOP)
                    .triggerableAnim("enable", Enable)
                    .triggerableAnim("disable", DISABLE)
            );
        }

        @Override
        public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
            super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if(!isToggling)return;
            if (level instanceof ServerLevel serverlevel) {
                isEnable = switchVisorState(player);
                if (isEnable) {
                    triggerArmorAnim(player, GeoItem.getOrAssignId(helmet, serverlevel), "controller", "disable");
                } else {
                    triggerArmorAnim(player, GeoItem.getOrAssignId(helmet, serverlevel), "controller", "enable");
                }
                setToggling(false);
            }
            ZeroContactLogger.LOG.info("isEnable:{}",isEnable);
        }


        @Override
        public ResourceLocation getVisorTexture() {
            return visor;
        }

        @Override
        public boolean getEnabled() {
            return isEnable;
        }

        @Override
        public void setToggling(boolean toggling) {
            this.isToggling = toggling;
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }
    }
}
