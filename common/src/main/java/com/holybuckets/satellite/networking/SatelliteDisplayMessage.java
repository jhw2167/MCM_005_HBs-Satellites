package com.holybuckets.satellite.networking;

import net.minecraft.core.BlockPos;

public class SatelliteDisplayMessage {
    public static final String LOCATION = "satellite_display";
    public final BlockPos pos;
    public final int[] displayData;

    SatelliteDisplayMessage(BlockPos pos, int[] displayData) {
        this.pos = pos;
        this.displayData = displayData;
    }

    public static void createAndFire(BlockPos pos, int[] displayData) {
        if (displayData.length != 4096) {
            throw new IllegalArgumentException("Display data must be exactly 4096 integers");
        }
        SatelliteDisplayMessage message = new SatelliteDisplayMessage(pos, displayData);
        SatelliteDisplayMessageHandler.createAndFire(message);
    }
}
