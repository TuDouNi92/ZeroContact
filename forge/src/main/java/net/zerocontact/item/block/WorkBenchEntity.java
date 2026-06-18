package net.zerocontact.item.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.network.NetworkHandler;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.zerocontact.forge_registries.BlocksRegForge.WORKBENCH_ENTITY;


public class WorkBenchEntity extends BlockEntity implements GeoBlockEntity {
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation OPEN = RawAnimation.begin().then("laptop_on", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation CLOSED = RawAnimation.begin().then("laptop_off", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static List<GearRecipeData> recipeData = new ArrayList<>();

    public WorkBenchEntity(BlockPos pos, BlockState blockState) {
        super(WORKBENCH_ENTITY.get(), pos, blockState);
    }

    public static void buy(NetworkHandler.BuyGearsPacket msg, ServerPlayer player) {
        BlockEntity be = player.level().getBlockEntity(msg.pos());
        if (!(be instanceof WorkBenchEntity workBenchEntity)) return;
        ResourceLocation gearKey = ForgeRegistries.ITEMS.getKey(msg.gearItem());
        if (gearKey == null) return;
        Optional<GearRecipeData> gearRecipeData = recipeData.stream().filter(data -> data.gearId.equals(gearKey.toString())).findFirst();
        if (gearRecipeData.isEmpty()) return;
        if (workBenchEntity.canTrade(gearRecipeData.get(), player)) {
            workBenchEntity.consumeItems(gearRecipeData.get(), player);
            workBenchEntity.giveItem(player, gearKey.toString());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "laptop", this::predicate)
                .triggerableAnim("open", OPEN)
                .triggerableAnim("close", CLOSED)
        );
    }

    private PlayState predicate(AnimationState<WorkBenchEntity> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean canTrade(GearRecipeData data, ServerPlayer player) {
        if (player.isCreative()) return true;
        Inventory inv = player.getInventory();
        for (GearRecipeData.IngredientItems req : data.ingredientItems) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.itemId));
            if (item == null) return false;
            int total = 0;
            for (ItemStack stack : inv.items) {
                if (stack.getItem() == item) {
                    total += stack.getCount();
                }
            }
            if (total < req.neededCount) {
                return false;
            }
        }
        return true;
    }

    public void consumeItems(GearRecipeData data, ServerPlayer player) {
        if (player.isCreative()) return;
        Inventory inv = player.getInventory();
        for (GearRecipeData.IngredientItems req : data.ingredientItems) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.itemId));
            if (item == null) continue;
            int remaining = req.neededCount;
            for (ItemStack stack : inv.items) {
                if (stack.getItem() == item && remaining > 0) {
                    int remove = Math.min(stack.getCount(), remaining);
                    stack.shrink(remove);
                    remaining -= remove;
                }
                if (remaining <= 0) break;
            }
        }
        inv.setChanged();
    }

    public void giveItem(ServerPlayer player, String id) {
        Item targetItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (targetItem == null) return;
        ItemStack stack = new ItemStack(targetItem);
        stack.setCount(stack.getMaxStackSize());
        player.getInventory().placeItemBackInInventory(stack, true);
        player.inventoryMenu.broadcastChanges();
    }
}
