package com.holybuckets.satellite.block.be.isatelliteblocks;

import net.minecraft.core.BlockPos;

public interface ITargetController {
    
    void setUiPosition(BlockPos blockPos);
    
    BlockPos getUiPosition();
    
    int getTargetColorId();
}
