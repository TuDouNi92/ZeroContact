package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;
import net.zerocontact.ZeroContact;
import net.zerocontact.capability.CapabilityRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class GenerateModuleGeoImpl extends AbstractGenerateGeoCurioItemImpl implements EquipmentModule {
    private static final ResourceLocation NAVBOARD_TRAIT = new ResourceLocation(ZeroContact.MOD_ID, "navboard");
    private static final RawAnimation OPEN = RawAnimation.begin().then("open", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation CLOSE = RawAnimation.begin().then("close", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private final ResourceLocation moduleTrait;

    public GenerateModuleGeoImpl(int defaultDurability, ResourceLocation moduleTrait, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super("", defaultDurability, texture, model, animation, Type.CHESTPLATE);
        this.moduleTrait = moduleTrait;
    }

    @Override
    public ResourceLocation getModuleTrait() {
        return moduleTrait;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        if (!NAVBOARD_TRAIT.equals(moduleTrait)) return;
        registrar.add(new AnimationController<>(this, "navboard", 0, state -> {
            if (state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI) {
                return PlayState.STOP;
            }
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack == null) return PlayState.STOP;
            boolean enabled = stack.getCapability(CapabilityRegistries.NAV_BOARD)
                    .map(NavBoardContainer::isEnabled).orElse(false);
            // Reusing the same RawAnimation holds its last frame; only state changes switch clips.
            state.getController().setAnimation(enabled ? OPEN : CLOSE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public boolean isPerspectiveAware() {
        // Also safe while the superclass creates the animation cache, before moduleTrait is assigned.
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                          @NotNull InteractionHand usedHand) {
        ModuleRegistry.CapabilityEntry trait = ModuleRegistry.getTrait(getModuleTrait());
        if (trait != null && trait.useHandler().isPresent()) {
            return trait.useHandler().get().use(level, player, usedHand);
        }
        return super.use(level, player, usedHand);
    }
}
