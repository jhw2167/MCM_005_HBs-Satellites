package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Hologram display blocks understand their chunk display info to display
 * Block entity implements onTick info of setting chisel block above it
 */
public interface ISatelliteDisplayBE {

    void forceUpdate();

    void toggleOnOff(boolean toggle);

    SatelliteDisplay getSource();

    void setSource(SatelliteDisplay source, boolean forceUpdate);

    BlockPos getBlockPos();

    //0-4095
    static int getCachePos(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }

    //0-1,0-1,0-1
    static float CONST = 0.0625f;
    static Vec3 get3Dpos(int x, int y, int z) {
        return new Vec3(x*CONST, y*CONST, z*CONST);
    }

    boolean isDisplayOn();

    int getHeight();

    /*
    static final int DIST_THRESHOLD_SQR = 16*16;
    static void handleClientUpdate(Player player, SatelliteDisplayMessage message) {
        if( HBUtil.BlockUtil.distanceSqr(player.blockPosition(), message.pos) > DIST_THRESHOLD_SQR)
            return;
        BlockEntity be = player.level().getBlockEntity(message.pos);
        if( be instanceof ISatelliteDisplayBlock display ) {
            display.updateClient( new SatelliteDisplayUpdate(message) );
        }
    }
     */

}
