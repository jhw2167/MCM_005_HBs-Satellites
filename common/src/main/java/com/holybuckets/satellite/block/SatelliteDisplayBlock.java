package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SatelliteDisplayBlock extends Block implements EntityBlock {
    
    public SatelliteDisplayBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteDisplayBlockEntity(pos, state);
    }
}
