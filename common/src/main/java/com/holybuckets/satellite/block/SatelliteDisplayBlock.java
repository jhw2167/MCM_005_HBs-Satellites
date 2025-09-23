package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SatelliteDisplayBlock extends Block implements EntityBlock {
    
    public SatelliteDisplayBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteDisplayBlockEntity(pos, state);
    }

    //Override block is destroyed
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof SatelliteDisplayBlockEntity) {
            SatelliteDisplayBlockEntity displayBE = (SatelliteDisplayBlockEntity) be;
            displayBE.onDestroyed();
        }
        super.destroy(level, pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, pos, s, blockEntity) -> ((SatelliteDisplayBlockEntity) blockEntity).tick(l, pos, state, (SatelliteDisplayBlockEntity) blockEntity);
    }
}
