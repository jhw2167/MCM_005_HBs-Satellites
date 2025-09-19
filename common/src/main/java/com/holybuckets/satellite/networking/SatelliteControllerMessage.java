package com.holybuckets.satellite.networking;

import net.minecraft.core.BlockPos;

public class SatelliteControllerMessage {
    public static final String LOCATION = "controller_input";
    /** Incoming client to server to move chunks */
    public final int controllerInput;
    /** Outgoing, triggers display on block on or off */
    public final int useDisplay;
    /** Where to put the display **/
    public final BlockPos pos;

    SatelliteControllerMessage(int controllerInput, int useDisplay, BlockPos pos) {
        this.controllerInput = controllerInput;
        this.useDisplay = useDisplay;
        this.pos = pos;
    }

    SatelliteControllerMessage(int useDisplay, BlockPos pos) {
        this.controllerInput = -1;
        this.useDisplay = useDisplay;
        this.pos = pos;
    }

    public static void createAndFire(int controllerInput, int useDisplay, BlockPos pos) {
        SatelliteControllerMessage message = new SatelliteControllerMessage(controllerInput, useDisplay, pos);
        SatelliteControllerMessageHandler.createAndFire(message);
    }
}
