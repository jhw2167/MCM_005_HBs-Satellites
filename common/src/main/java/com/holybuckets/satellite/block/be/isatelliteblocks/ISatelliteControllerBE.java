package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Controller block has a color id set by the player and can link to a satellite
 */
public interface ISatelliteControllerBE extends ISatelliteDisplayBE {

    BlockPos getUiPosition();

    int getColorId();

    void setColorId(int colorId);

    void setSource(SatelliteDisplay display);


    float ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD = 0.7f;
    float ORDINAL_COORD_BLOCK_HORZ_LEFT_THRESHOLD = 0.3f;
    float ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD = 0.8f;
    float ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD = 0.2f;
    float ORDINAL_COORD_BUFFER_V = 0.25f;
    float ORDINAL_COORD_BUFFER_H = 0.25f;

    /**
     * Return int command value based off where the block was struck
     *  - -1 = no command
     *  - 0 = toggle on/off
     *  - 1-4 = move satellite ordinally North (-1 Z), South (+1 Z), West (-1 X), East (+1 X)
     *  - 5-6 = adj satellite scanning section (+1 Y) or down (-1 Y)
     *  - 7-8 = adj display depth (+1) to a max of 4 then back to 1
     *  - 9 = adj to next wool color
     *  - 10-15 = undefined
     *  - 16+ = selects different wool color
     * @param res
     * @return
     */
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
        boolean isRightColumn = (xz > ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD);
        boolean isTopSection = y > ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD;
        boolean isBotSection = y < ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD;

        if (isRightColumn)
        {
             if(y > ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD) {
                 //nothing, block position area
             }
            else if(y > ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD) {
                //Chunk section depth up down
                float diff = ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD - ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD;
                cmd = (y > ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD + diff / 2) ? 5 : 6;
            } else {
                cmd = 7; //Display height adjust
            }

        } else if (isTopSection) {
            //Nothing, block position area

        } else if (isBotSection) {

            if (xz > ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD ) {
                cmd = 7; //Display height adjust - prob never reaches
            } else if( xz < ORDINAL_COORD_BLOCK_HORZ_LEFT_THRESHOLD ) {
                cmd = 0; //Toggle on/off
            } else {    //in the middle
                cmd = 16; //Change wool color
            }

        } else { // Quadrant 4:  Four directional panels (N/S/E/W)
            double upThreshold = ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD - ORDINAL_COORD_BUFFER_V;
            double downThreshold = ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD + ORDINAL_COORD_BUFFER_V;
            double leftThreshold = ORDINAL_COORD_BUFFER_H;
            double rightThreshold = ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD - ORDINAL_COORD_BUFFER_H;
            int input = 0;

            if(y > upThreshold) {
                input = 1; // Top Arrow
            } else if (y < downThreshold) {
                input = 2; // Bottom Arrow
            } else if (xz < leftThreshold) {
                input = 3; // Left Arrow
            } else if (xz > rightThreshold) {
                input = 4; // Right Arrow
            }

            /**
             * Up arrow should move OPPOSITE direction the block is facing, down arrow should move towards, side arrows fill respectively
             */
            if( blockFacing == Direction.NORTH ) {
                if(input == 1) cmd = 2; //Up arrow moves South
                else if(input == 2) cmd = 1; //Down arrow moves North
                else if(input == 3) cmd = 4; //left moves East
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
