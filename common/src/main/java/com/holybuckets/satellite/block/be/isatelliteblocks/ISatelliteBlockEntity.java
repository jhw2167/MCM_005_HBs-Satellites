package com.holybuckets.satellite.block.be.isatelliteblocks;

/**
 * Satellite block entities must know their chunkId and colorId
 */
public interface ISatelliteBlockEntity {

    int getColorId();

    void setColorId(int colorId);

    String getChunkId();

    void setChunkId(String chunkId);

}
