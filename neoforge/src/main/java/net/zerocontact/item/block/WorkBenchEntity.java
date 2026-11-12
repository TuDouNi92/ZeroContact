package net.zerocontact.item.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zerocontact.datagen.model.RecipePOJO;
import net.zerocontact.network.c2s.BuyGearsPacket;
import software.bernie.geckolib.animatable.GeoBlockEntity;

import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.zerocontact.forge_registries.BlockRegistry.WORKBENCH_ENTITY;


public class WorkBenchEntity extends BlockEntity implements GeoBlockEntity {
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation OPEN = RawAnimation.begin().then("laptop_on", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation CLOSED = RawAnimation.begin().then("laptop_off", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static List<RecipePOJO> recipeData = new ArrayList<>();

    public WorkBenchEntity(BlockPos pos, BlockState blockState) {
        super(WORKBENCH_ENTITY.get(), pos, blockState);
    }

    public static void buy(BuyGearsPacket msg, ServerPlayer player) {
        BlockEntity be = player.level().getBlockEntity(msg.pos());
        if (!(be instanceof WorkBenchEntity workBenchEntity)) return;
        ResourceLocation gearKey = BuiltInRegistries.ITEM.getKey(msg.gearItem());
        Optional<RecipePOJO> gearRecipeData = recipeData.stream().filter(data -> data.gearId.equals(gearKey.toString())).findFirst();
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

    public boolean canTrade(RecipePOJO data, ServerPlayer player) {
        if (player.isCreative()) return true;
        Inventory inv = player.getInventory();
        for (RecipePOJO.IngredientItems req : data.ingredientItems) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(req.itemId));
            if(item == Items.AIR)continue;
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

    public void consumeItems(RecipePOJO data, ServerPlayer player) {
        if (player.isCreative()) return;
        Inventory inv = player.getInventory();
        for (RecipePOJO.IngredientItems req : data.ingredientItems) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(req.itemId));
            if (item == Items.AIR)continue;
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
        Item targetItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        if(targetItem == Items.AIR)return;
        ItemStack stack = new ItemStack(targetItem);
        stack.setCount(stack.getMaxStackSize());
        player.getInventory().placeItemBackInInventory(stack, true);
        player.inventoryMenu.broadcastChanges();
    }
}
