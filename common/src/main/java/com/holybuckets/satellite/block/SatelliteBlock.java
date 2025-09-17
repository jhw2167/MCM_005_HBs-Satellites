package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SatelliteBlock extends Block implements EntityBlock {
    
    public SatelliteBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteBlockEntity(pos, state);
    }
}
