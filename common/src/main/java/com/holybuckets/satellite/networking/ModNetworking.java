package com.holybuckets.satellite.networking;

import com.holybuckets.satellite.Constants;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.network.BalmNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ModNetworking {

    public static String CLASS_ID = "014";

    private static int RECEIVED = 0;
    //private static ThreadPoolExecutor POOL = new ThreadPoolExecutor(2, 2, 60L, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.LinkedBlockingQueue<Runnable>());

    public static void initialize() {
        BalmNetworking bn = Balm.getNetworking();
        bn.registerServerboundPacket(id(SatelliteControllerMessage.LOCATION), SatelliteControllerMessage.class, Codecs::encodeControllerInput, Codecs::decodeControllerInput, ModNetworking::handleSatelliteControllerInput);
        bn.registerClientboundPacket(id(SatelliteControllerMessage.LOCATION), SatelliteControllerMessage.class, Codecs::encodeControllerInput, Codecs::decodeControllerInput, ModNetworking::handleSatelliteControllerInput);
        bn.registerClientboundPacket(id(SatelliteDisplayMessage.LOCATION), SatelliteDisplayMessage.class, Codecs::encodeControllerDisplay, Codecs::decodeControllerDisplay, ModNetworking::handleSatelliteDisplay);
    }

    private static ResourceLocation id(String location) {
        return new ResourceLocation(Constants.MOD_ID, location);
    }

    public static void handleSatelliteControllerInput(Player p, SatelliteControllerMessage m) {
        RECEIVED++;
        SatelliteControllerMessageHandler.handle(p, m);
        //POOL.submit(() -> ControllerInputMessageHandler.handle(p, m));
    }

    public static void handleSatelliteDisplay(Player p, SatelliteDisplayMessage m) {
        RECEIVED++;
        SatelliteDisplayMessageHandler.handle(p, m);
        //POOL.submit(() -> ControllerDisplayMessageHandler.handle(p, m));
    }


}
