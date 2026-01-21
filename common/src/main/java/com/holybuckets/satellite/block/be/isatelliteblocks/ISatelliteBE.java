package com.holybuckets.satellite.block.be.isatelliteblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Satellite block entities must know their chunkId and colorId
 */
public interface ISatelliteBE {

    int getColorId();

    void setColorId(int colorId);

    // Additional methods needed by SatelliteScreen
    BlockPos getTargetPos();
    
    void setTargetPos(BlockPos targetPos);
    
    BlockPos getBlockPos();
    
    boolean isTraveling();
    
    void launch(BlockPos targetPos);
    
    Level getLevel();

    default String getSatelliteDisplayError() {
        return null;
    }
}
