package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.RedStoneWireBlock.POWER;

public class SatelliteDisplayBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    
    public SatelliteDisplayBlock() {
        super(Properties.of()
            .lightLevel(state -> state.getValue(POWERED) ? 8 : 0)
            .sound(SoundType.METAL)
            .destroyTime(0.6f)  // Makes it break faster
            .explosionResistance(6f));
        registerDefaultState(this.stateDefinition.any()
            .setValue(POWERED, false)
            .setValue(POWER, 0)
            );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED)
                .add(POWER);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteDisplayBlockEntity(pos, state);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    //Override block is destroyed
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player p) {
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof SatelliteDisplayBlockEntity) {
            SatelliteDisplayBlockEntity displayBE = (SatelliteDisplayBlockEntity) be;
            displayBE.onDestroyed();
        }
        super.playerWillDestroy(level, pos, state, p);
    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        int signal =  this.getSignal(state, level, pos, null);
        if (signal != state.getValue(POWER)) {
            level.setBlock(pos, state.setValue(POWER, signal), 3);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {

        return level.isClientSide ? null : (l, pos, s, blockEntity) -> ((SatelliteDisplayBlockEntity) blockEntity).tick(l, pos, state, (SatelliteDisplayBlockEntity) blockEntity);
    }
}
