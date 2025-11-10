package com.holybuckets.satellite.block.be.isatelliteblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface ITargetController {
    
    void setUiPosition(BlockPos blockPos);
    
    BlockPos getUiPosition();
    
    int getTargetColorId();

    @Nullable
    Vec3 getCursorPosition();

    @Nullable
    void setCursorPosition(Vec3 pos);
}
