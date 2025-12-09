package com.holybuckets.satellite.block.be.isatelliteblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface ITargetController {
    
    void setUiTargetBlockPos(BlockPos blockPos);
    
    BlockPos getUiTargetBlockPos();
    
    int getTargetColorId();

    @Nullable
    Vec3 getCursorPosition();

    @Nullable
    void setCursorPosition(Vec3 pos);
}
