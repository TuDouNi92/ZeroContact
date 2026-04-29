package net.zerocontact.item.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.network.NetworkHooks;
import net.zerocontact.client.menu.WorkbenchMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.zerocontact.ZeroContact.MOD_ID;

@SuppressWarnings("deprecation")

public class Workbench extends Block implements EntityBlock {
    public static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/models/block/laptop_screen.png");
    public static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/block/laptop.geo.json");
    public static final ResourceLocation animation = new ResourceLocation(MOD_ID, "animations/laptop.animation.json");
    private static final DirectionProperty FACING = BlockStateProperties.FACING;

    public Workbench() {
        super(
                Properties.of().strength(0.5f)
                        .explosionResistance(2)
                        .lightLevel(s -> 10)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof WorkBenchEntity workBenchEntity) {
            workBenchEntity.triggerAnim("laptop", "open");
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, __) -> new WorkbenchMenu(id, inv, pos), Component.empty()), buf -> this.sendBlockPos(buf, pos)
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void sendBlockPos(FriendlyByteBuf buf, BlockPos pos) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof WorkBenchEntity workBenchEntity) {
            workBenchEntity.triggerAnim("laptop", "close");
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WorkBenchEntity(pos, state);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (pos.getY() - 1 == neighborPos.getY() && level.isEmptyBlock(neighborPos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params) {
        return List.of(new ItemStack(asItem(), 1));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(2, 0, 4, 14, 4, 12);
    }
}
