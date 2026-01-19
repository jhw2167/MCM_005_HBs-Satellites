package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.UpgradeControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.RedStoneWireBlock.POWER;

public class UpgradeControllerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public UpgradeControllerBlock() {
        super(Properties.of()
            .lightLevel(state -> state.getValue(POWERED) ? 8 : 0)
            .sound(SoundType.METAL)
            .destroyTime(0.6f)
            .explosionResistance(6f));
        registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(POWERED);
        builder.add(POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(POWERED, false)
            .setValue(POWER, 0);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UpgradeControllerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction d = hitResult.getDirection();
        Direction front = state.getValue(FACING);
        if (d != front) { return InteractionResult.PASS; }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof UpgradeControllerBlockEntity controller) {
            controller.use(player, hand, hitResult);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void destroy(LevelAccessor $$0, BlockPos $$1, BlockState $$2) {
        super.destroy($$0, $$1, $$2);
        //call destroy on block entity
        BlockEntity be = $$0.getBlockEntity($$1);
        if (be instanceof UpgradeControllerBlockEntity controller) {
            controller.onDestroy();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, pos, s, blockEntity) -> ((SatelliteDisplayBlockEntity) blockEntity).tick(l, pos, state, (SatelliteDisplayBlockEntity) blockEntity);
    }
}
