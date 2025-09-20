package com.holybuckets.satellite.networking;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.minecraft.world.entity.player.Player;

public class SatelliteDisplayMessageHandler {
    public static String CLASS_ID = "017";

    public static void createAndFire(SatelliteDisplayMessage message) {
        HBUtil.NetworkUtil.serverSendToAllPlayers(message);
    }

    public static void handle(Player player, SatelliteDisplayMessage message) {
        ISatelliteDisplayBlock.handleClientUpdate(player, message);
    }
}
