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
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

import static net.zerocontact.forge_registries.BlocksRegForge.WORKBENCH_ENTITY;


public class WorkBenchEntity extends BlockEntity implements GeoBlockEntity {
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation OPEN = RawAnimation.begin().then("laptop_on", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation CLOSED = RawAnimation.begin().then("laptop_off", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static List<GearRecipeData> recipeData = new ArrayList<>();

    public WorkBenchEntity(BlockPos pos, BlockState blockState) {
        super(WORKBENCH_ENTITY.get(), pos, blockState);
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

    public boolean canCraft(GearRecipeData data, ServerPlayer player) {
        Inventory inv = player.getInventory();
        for (GearRecipeData.IngredientItems req : data.ingredientItems) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.itemId));
            if (item == null) return false;
            int total = 0;
            for (ItemStack stack : inv.items) {
                if (stack.getItem() == item && stack.getCount() >= req.neededCount) {
                    total += stack.getCount();
                    //待处理同类物品分槽的判定
                }
            }
            if (total < req.neededCount) {
                return false;
            }
        }
        return true;
    }

    public void consumeItems(GearRecipeData data, ServerPlayer player) {
        if (canCraft(data, player)) {
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
    }
    public void giveItem(ServerPlayer player, String id){
        Item targetItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if(targetItem==null)return;
        player.getInventory().placeItemBackInInventory(new ItemStack(targetItem,1),true);
        player.inventoryMenu.broadcastChanges();
    }
}
