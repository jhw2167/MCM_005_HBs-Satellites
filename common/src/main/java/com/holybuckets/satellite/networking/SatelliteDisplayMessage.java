package com.holybuckets.satellite.networking;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class SatelliteDisplayMessage implements CustomPacketPayload {
    public static final String LOCATION = "satellite_display";

    // 1.21.1-style packet identity and codec.
    public static final CustomPacketPayload.Type<SatelliteDisplayMessage> TYPE =
        new CustomPacketPayload.Type<>(HBUtil.LOC(Constants.MOD_ID, LOCATION));

    public static final StreamCodec<RegistryFriendlyByteBuf, SatelliteDisplayMessage> STREAM_CODEC =
        CustomPacketPayload.codec(Codecs::encodeControllerDisplay, Codecs::decodeControllerDisplay);

    public final BlockPos pos;
    public final int[] displayData;
    public final int height;

    SatelliteDisplayMessage(BlockPos pos, int[] displayData, int height) {
        this.pos = pos;
        this.displayData = displayData;
        this.height = height;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void createAndFire(BlockPos pos, int[] displayData, int height) {
        if (displayData.length != 4096) {
            throw new IllegalArgumentException("Display data must be exactly 4096 integers");
        }
        SatelliteDisplayMessage message = new SatelliteDisplayMessage(pos, displayData, height);
        SatelliteDisplayMessageHandler.createAndFire(message);
    }
}
