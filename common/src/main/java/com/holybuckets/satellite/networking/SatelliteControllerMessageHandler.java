package com.holybuckets.satellite.networking;

import com.holybuckets.foundation.HBUtil;

public class SatelliteControllerMessageHandler {
    public static String CLASS_ID = "016";

    static void createAndFire(SatelliteControllerMessage message) {
        if(message.controllerInput == -1) {
            HBUtil.NetworkUtil.serverSendToAllPlayers(message);
        } else {
            HBUtil.NetworkUtil.clientSendToServer(message);
        }

    }

    /*
    static void handle(Player player, SatelliteControllerMessage message) {
        //Get bock entity at blockPos, cast to ISatelliteDisplay and call updateClient
        GeneralConfig config = GeneralConfig.getInstance();
        SatelliteDisplayUpdate update = new SatelliteDisplayUpdate(message);
        if( config.isServerSide() ) {
            BlockEntity be = player.level().getBlockEntity(message.pos);
            if( be instanceof ISatelliteControllerBlock controller ) {
                controller.updateServer(update);
            }
        } else {
            BlockEntity be = player.level().getBlockEntity(message.pos);
            if( be instanceof ISatelliteDisplayBlock display ) {
                display.updateClient(update);
            }
        }

    }
    */

}
