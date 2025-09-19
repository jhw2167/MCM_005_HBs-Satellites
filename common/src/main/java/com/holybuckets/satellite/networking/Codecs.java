package com.holybuckets.satellite.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class Codecs {

    public static final FriendlyByteBuf encodeControllerInput(SatelliteControllerMessage object, FriendlyByteBuf buf) {
        buf.writeInt(object.controllerInput);
        buf.writeInt(object.useDisplay);
        buf.writeBlockPos(object.pos);
        return buf;
    }

    public static final SatelliteControllerMessage decodeControllerInput(FriendlyByteBuf buf) {
        int controllerInput = buf.readInt();
        int useDisplay = buf.readInt();
        BlockPos pos = buf.readBlockPos();
        return new SatelliteControllerMessage(controllerInput, useDisplay, pos);
    }

    public static final FriendlyByteBuf encodeControllerDisplay(SatelliteDisplayMessage object, FriendlyByteBuf buf) {
        buf.writeBlockPos(object.pos);
        buf.writeVarIntArray(object.displayData);
        return buf;
    }

    public static final SatelliteDisplayMessage decodeControllerDisplay(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int[] displayData = buf.readVarIntArray();
        return new SatelliteDisplayMessage(pos, displayData);
    }


}
