package com.holybuckets.satellite.networking;

import net.minecraft.core.BlockPos;

public class SatelliteDisplayMessage {
    public static final String LOCATION = "satellite_display";
    public final BlockPos pos;
    public final int[] displayData;
    public final int height;

    SatelliteDisplayMessage(BlockPos pos, int[] displayData, int height) {
        this.pos = pos;
        this.displayData = displayData;
        this.height = height;
    }

    public static void createAndFire(BlockPos pos, int[] displayData, int height) {
        if (displayData.length != 4096) {
            throw new IllegalArgumentException("Display data must be exactly 4096 integers");
        }
        SatelliteDisplayMessage message = new SatelliteDisplayMessage(pos, displayData, height);
        SatelliteDisplayMessageHandler.createAndFire(message);
    }
}
