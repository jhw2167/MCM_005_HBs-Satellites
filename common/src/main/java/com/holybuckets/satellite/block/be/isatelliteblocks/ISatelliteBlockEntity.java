package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

/**
 * Satellite block entities must know their chunkId and colorId
 */
public interface ISatelliteBlockEntity  {

    int getColorId();

    void setColorId(int colorId);

}
