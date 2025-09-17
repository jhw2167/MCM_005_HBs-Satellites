package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.core.ChunkDisplayInfo;

/**
 * Hologram display blocks understand their chunk display info to display
 * Block entity implements onTick info of setting chisel block above it
 */
public interface IHologramDisplayBlock {

    void setDisplayInfo(ChunkDisplayInfo displayInfo);

    void toggleOnOff(boolean toggle);

}
