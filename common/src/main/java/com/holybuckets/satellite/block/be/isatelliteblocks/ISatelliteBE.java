package com.holybuckets.satellite.block.be.isatelliteblocks;

/**
 * Satellite block entities must know their chunkId and colorId
 */
public interface ISatelliteBE {

    int getColorId();

    void setColorId(int colorId);

}
