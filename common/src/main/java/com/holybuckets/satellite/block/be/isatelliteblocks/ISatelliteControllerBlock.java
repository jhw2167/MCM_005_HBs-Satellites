package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Controller block has a color id set by the player and can link to a satellite
 */
public interface ISatelliteControllerBlock extends ISatelliteDisplayBlock {

    int getColorId();

    void setColorId(int colorId);

    void setSatellite(SatelliteBlockEntity satellite);

    void updateServer(SatelliteDisplayUpdate update);



    float ORDINAL_COORD_BLOCK_HORZ_THRESHOLD = 0.7f;
    float ORDINAL_COORD_BLOCK_VERT_THRESHOLD = 0.8f;
    float ORDINAL_COORD_BUFFER_V = 0.25f;
    float ORDINAL_COORD_BUFFER_H = 0.25f;

    static int calculateHitCommand(BlockHitResult res)
    {
        int cmd = -1;
        BlockPos p = res.getBlockPos();
        Vec3 blockCoord = res.getLocation().subtract(p.getX(), p.getY(), p.getZ());
        Direction blockFacing = res.getDirection();
        if(blockFacing == Direction.UP || blockFacing == Direction.DOWN) return cmd;

        // Get relative coordinates within the block (0.0 to 1.0)
        double xz = blockCoord.x;
        double y = blockCoord.y;
        double z = blockCoord.z;

        //block always exists in xy or yz plane, x and z are mutually exclusive
        if (blockFacing == Direction.EAST || blockFacing == Direction.WEST) {
            xz = z;
        }
        // Determine which quadrant was hit
        boolean isRightSide = (xz > ORDINAL_COORD_BLOCK_HORZ_THRESHOLD);
        boolean isTopHalf = y > ORDINAL_COORD_BLOCK_VERT_THRESHOLD;

        if (isTopHalf && isRightSide) {
            // Quadrant 3: Top right - Toggle on/off button
            cmd = 0;

        } else if (isTopHalf && !isRightSide) {
            // Quadrant 4: Top left - Color change (pending implementation)
            cmd = -1; // No logic implemented yet

        } else if (!isTopHalf && isRightSide) {
            // Quadrant 2: Bottom right - Up/Down arrows only
            if (y > 0.4) { // Upper half of bottom right quadrant
                cmd = 5; // Up arrow
            } else { // Lower half of bottom right quadrant
                cmd = 6; // Down arrow
            }

        } else {
            // Quadrant 1: Bottom left - Four directional panels (N/S/E/W)
            // Divide this quadrant into 4 even sections

            double upThreshold = ORDINAL_COORD_BLOCK_VERT_THRESHOLD - ORDINAL_COORD_BUFFER_V;
            double downThreshold = ORDINAL_COORD_BUFFER_V;
            double leftThreshold = ORDINAL_COORD_BUFFER_H;
            double rightThreshold = ORDINAL_COORD_BLOCK_HORZ_THRESHOLD - ORDINAL_COORD_BUFFER_H;
            int input = 0;

            if(y > upThreshold) {
                input = 1; // Top Arrow
            } else if (y < downThreshold) {
                input = 2; // South
            } else if (xz < leftThreshold) {
                input = 3; // West
            } else if (xz > rightThreshold) {
                input = 4; // East
            }

            /**
             * Up arrow should move OPPOSITE direction the block is facing, down arrow should move towards, side arrows fill respectively
             */
            if( blockFacing == Direction.NORTH ) {
                if(input == 1) cmd = 2; //Up arrow moves South
                else if(input == 2) cmd = 1; //Down arrow moves North
                else if(input == 3) cmd = 4; //West moves West
                else if(input == 4) cmd = 3; //East moves East
            } else if( blockFacing == Direction.SOUTH ) {
                if(input == 1) cmd = 1; //Up arrow moves North
                else if(input == 2) cmd = 2; //Down arrow moves South
                else if(input == 3) cmd = 3; //West moves East
                else if(input == 4) cmd = 4; //East moves West
            } else if( blockFacing == Direction.EAST ) {
                if(input == 1) cmd = 4; //Up arrow moves West
                else if(input == 2) cmd = 3; //Down arrow moves East
                else if(input == 3) cmd = 1; //West moves North
                else if(input == 4) cmd = 2; //East moves South
            } else if( blockFacing == Direction.WEST ) {
                if(input == 1) cmd = 3; //Up arrow moves East
                else if(input == 2) cmd = 4; //Down arrow moves West
                else if(input == 3) cmd = 2; //West moves South
                else if(input == 4) cmd = 1; //East moves North
            }


        }

        return cmd;
    }

}
