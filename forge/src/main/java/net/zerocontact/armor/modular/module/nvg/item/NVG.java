package net.zerocontact.armor.modular.module.nvg.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public abstract class NVG extends AbstractGenerateGeoCurioItemImpl implements EquipmentModule, INvg {
    protected static final ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, "textures/models/nvg/nvg_pvs31.png");
    protected static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/nvg/nvg_pvs31.geo.json");
    protected static final ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, "animations/nvg_pvs31.animation.json");
    protected static final RawAnimation ACTIVATE = RawAnimation.begin().then("activate", Animation.LoopType.HOLD_ON_LAST_FRAME);
    protected static final RawAnimation DEACTIVATE = RawAnimation.begin().then("deactivate", Animation.LoopType.HOLD_ON_LAST_FRAME);

    private static final ResourceLocation vignette = new ResourceLocation("");
    private static final ResourceLocation trait = new ResourceLocation(ZeroContact.MOD_ID, "nvg");

    public NVG(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, ArmorItem.Type armorType) {
        super(id, defaultDurability, texture, model, animation, armorType);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(level.isClientSide)return;
        stack.getCapability(CapabilityRegistries.NVG).ifPresent(nvg -> this.setDamage(stack, (int) (nvg.getDefaultBattery() - nvg.getBattery())));
    }


    @Override
    public ResourceLocation getVignette() {
        return vignette;
    }

    @Override
    public ResourceLocation getAnimation() {
        return animation;
    }

    @Override
    public ResourceLocation getModuleTrait() {
        return trait;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar
                .add(
                        new AnimationController<>(
                                this,
                                "controller",
                                0,
                                state -> {
                                    if (state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI) {
                                        return PlayState.STOP;
                                    }
                                    return PlayState.CONTINUE;
                                })
                                // The local manager installs an instance-scoped handler before triggering.
                                .setCustomInstructionKeyframeHandler(event -> {
                                })
                                .receiveTriggeredAnimations()
                                .triggerableAnim("on_pose", RawAnimation.begin().then("on_pose", Animation.LoopType.HOLD_ON_LAST_FRAME))
                                .triggerableAnim("off_pose", RawAnimation.begin().then("off_pose", Animation.LoopType.HOLD_ON_LAST_FRAME))
                                .triggerableAnim("activate", ACTIVATE)
                                .triggerableAnim("deactivate", DEACTIVATE)
                );
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

}
