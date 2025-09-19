package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;
import com.holybuckets.satellite.networking.SatelliteDisplayMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Hologram display blocks understand their chunk display info to display
 * Block entity implements onTick info of setting chisel block above it
 */
public interface ISatelliteDisplayBlock {

    void toggleOnOff(boolean toggle);

    SatelliteDisplay getSource();

    void setSource(SatelliteDisplay source);

    BlockPos getBlockPos();

    void updateClient(SatelliteDisplayUpdate update);

    //0-4095
    static int getCachePos(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }


    static void handleClientUpdate(Player p, SatelliteDisplayMessage msg) {
    }

}
