package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SatelliteBlockEntity extends BlockEntity implements ISatelliteBlockEntity
{
    int colorId;
    String chunkId;

    public SatelliteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteBlockEntity.get(), pos, state);
    }

    @Override
    public int getColorId() {
        return 0;
    }

    @Override
    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    @Override
    public String getChunkId() {
        return chunkId;
    }

    @Override
    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }
}
