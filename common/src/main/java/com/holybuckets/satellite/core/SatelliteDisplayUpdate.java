package com.holybuckets.satellite.core;

import com.holybuckets.satellite.networking.SatelliteDisplayMessage;
import com.holybuckets.satellite.networking.SatelliteControllerMessage;
import net.minecraft.core.BlockPos;

/**
 * Wrapper message for SatelliteControllerMessage and ControllerDisplayMessage
 */
public class SatelliteDisplayUpdate {

    SatelliteControllerMessage controllerMessage;
    SatelliteDisplayMessage displayMessage;

    public BlockPos pos;
    public boolean displayOn;
    public int[] displayData;

    //One constructor for each type
    public SatelliteDisplayUpdate(SatelliteControllerMessage msg) {
        this.controllerMessage = msg;
        this.pos = msg.pos;
        this.displayOn = msg.useDisplay != 0;
    }

    public SatelliteDisplayUpdate(SatelliteDisplayMessage msg) {
        this.displayMessage = msg;
        this.pos = msg.pos;
        this.displayData = msg.displayData;
    }


}
