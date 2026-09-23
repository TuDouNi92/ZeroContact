package net.zerocontact.api.armor.modular;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Handles use of a module item on both logical sides, just like Item.use.
 * Implementations are shared by all items using the trait; keep per-stack state
 * in the stack or its capabilities and guard server-only actions accordingly.
 */
@FunctionalInterface
public interface ModuleUseHandler {
    @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                   @NotNull InteractionHand hand);
}
