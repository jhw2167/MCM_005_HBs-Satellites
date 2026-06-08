package com.holybuckets.satellite.networking;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class SatelliteControllerMessage implements CustomPacketPayload {
    public static final String LOCATION = "controller_input";

    // 1.21.1-style packet identity and codec.
    public static final CustomPacketPayload.Type<SatelliteControllerMessage> TYPE =
        new CustomPacketPayload.Type<>(HBUtil.LOC(Constants.MOD_ID, LOCATION));

    public static final StreamCodec<RegistryFriendlyByteBuf, SatelliteControllerMessage> STREAM_CODEC =
        CustomPacketPayload.codec(Codecs::encodeControllerInput, Codecs::decodeControllerInput);

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

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void createAndFire(int controllerInput, int useDisplay, BlockPos pos) {
        SatelliteControllerMessage message = new SatelliteControllerMessage(controllerInput, useDisplay, pos);
        SatelliteControllerMessageHandler.createAndFire(message);
    }

    public static void createAndFire(int useDisplay, BlockPos pos) {
        SatelliteControllerMessage message = new SatelliteControllerMessage(useDisplay, pos);
        SatelliteControllerMessageHandler.createAndFire(message);
    }
}
