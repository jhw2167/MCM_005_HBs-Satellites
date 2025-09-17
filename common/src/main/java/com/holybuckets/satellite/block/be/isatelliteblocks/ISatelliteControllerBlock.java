package com.holybuckets.satellite.block.be.isatelliteblocks;

/**
 * Controller block has a color id set by the player and can link to a satellite
 */
public interface ISatelliteControllerBlock extends IHologramDisplayBlock {

    int getColorId();

    void setColorId(int colorId);

    void setSatellite(ISatelliteBlockEntity satellite);

}
