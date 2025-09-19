package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;

/**
 * Controller block has a color id set by the player and can link to a satellite
 */
public interface ISatelliteControllerBlock extends ISatelliteDisplayBlock {

    int getColorId();

    void setColorId(int colorId);

    void setSatellite(SatelliteBlockEntity satellite);

    void updateServer(SatelliteDisplayUpdate update);

}
