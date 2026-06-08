package com.holybuckets.satellite.networking;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.network.BalmNetworking;
import net.minecraft.world.entity.player.Player;

public class ModNetworking {

    public static String CLASS_ID = "014";

    private static int RECEIVED = 0;
    //private static ThreadPoolExecutor POOL = new ThreadPoolExecutor(2, 2, 60L, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.LinkedBlockingQueue<Runnable>());

    // 1.21.1-style registration: TYPE + STREAM_CODEC live on each message class.
    public static void initialize() {
        BalmNetworking bn = Balm.getNetworking();

        bn.registerServerboundPacket(
            SatelliteControllerMessage.TYPE,
            SatelliteControllerMessage.class,
            SatelliteControllerMessage.STREAM_CODEC,
            ModNetworking::handleSatelliteControllerInput
        );

        bn.registerClientboundPacket(
            SatelliteControllerMessage.TYPE,
            SatelliteControllerMessage.class,
            SatelliteControllerMessage.STREAM_CODEC,
            ModNetworking::handleSatelliteControllerInput
        );

        bn.registerClientboundPacket(
            SatelliteDisplayMessage.TYPE,
            SatelliteDisplayMessage.class,
            SatelliteDisplayMessage.STREAM_CODEC,
            ModNetworking::handleSatelliteDisplay
        );
    }

    public static void handleSatelliteControllerInput(Player p, SatelliteControllerMessage m) {
        RECEIVED++;
        //SatelliteControllerMessageHandler.handle(p, m);
        //POOL.submit(() -> ControllerInputMessageHandler.handle(p, m));
    }

    public static void handleSatelliteDisplay(Player p, SatelliteDisplayMessage m) {
        RECEIVED++;
        SatelliteDisplayMessageHandler.handle(p, m);
        //POOL.submit(() -> ControllerDisplayMessageHandler.handle(p, m));
    }


}
