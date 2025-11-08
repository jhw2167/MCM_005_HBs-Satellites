package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.PositionControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PositionControllerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PositionControllerBlock() {
        super(Properties.of()
            .lightLevel(state -> state.getValue(POWERED) ? 8 : 0)
            .sound(SoundType.METAL)
            .destroyTime(0.6f)
            .explosionResistance(6f));
        registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(POWERED, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PositionControllerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction d = hitResult.getDirection();
        Direction front = state.getValue(FACING);
        if (d != front) { return InteractionResult.PASS; }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PositionControllerBlockEntity controller) {
            controller.use(player, hand, hitResult);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
