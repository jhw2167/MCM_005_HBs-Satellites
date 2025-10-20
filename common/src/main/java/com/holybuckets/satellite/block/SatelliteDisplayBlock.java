package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

public class SatelliteDisplayBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    
    public SatelliteDisplayBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK)
            .lightLevel(state -> state.getValue(POWERED) ? 15 : 0));
        registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteDisplayBlockEntity(pos, state);
    }

    //Override block is destroyed
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player p) {
        super.playerWillDestroy(level, pos, state, p);
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof SatelliteDisplayBlockEntity) {
            SatelliteDisplayBlockEntity displayBE = (SatelliteDisplayBlockEntity) be;
            displayBE.onDestroyed();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, pos, s, blockEntity) -> ((SatelliteDisplayBlockEntity) blockEntity).tick(l, pos, state, (SatelliteDisplayBlockEntity) blockEntity);
    }
}
