package com.holybuckets.satellite.block.be.isatelliteblocks;

import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Controller block has a color id set by the player and can link to a satellite
 */
public interface ISatelliteControllerBE extends ISatelliteDisplayBE {

    BlockPos getUiTargetBlockPos();

    int getColorId();

    void setColorId(int colorId);

    SatelliteControllerBlockEntity getSatelliteController();


    float ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD = 0.7f;
    float ORDINAL_COORD_BLOCK_HORZ_LEFT_THRESHOLD = 0.3f;
    float ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD = 0.8f;
    float ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD = 0.2f;
    float ORDINAL_COORD_BUFFER_V = 0.25f;
    float ORDINAL_COORD_BUFFER_H = 0.25f;

    /**
     * Return int command value based off where the block was struck
     * - -1 = no command
     * - 0 = toggle on/off
     * - 1-4 = move satellite ordinally North (-1 Z), South (+1 Z), West (-1 X), East (+1 X)
     * - 5-6 = adj satellite scanning section (+1 Y) or down (-1 Y)
     * - 7-8 = adj display depth (+1) to a max of 4 then back to 1
     * - 9 = adj to next wool color
     * - 10-15 = undefined
     * - 16+ = selects different wool color
     *
     * @param res
     * @return
     */
    static int calculateHitCommand(BlockHitResult res) {
        int cmd = -1;
        BlockPos p = res.getBlockPos();
        Vec3 blockCoord = res.getLocation().subtract(p.getX(), p.getY(), p.getZ());
        Direction blockFacing = res.getDirection();
        if (blockFacing == Direction.UP || blockFacing == Direction.DOWN) return cmd;

// Get relative coordinates within the block (0.0 to 1.0)
        double xz;
        double y = blockCoord.y;

// Map coordinates correctly based on facing direction
        if (blockFacing == Direction.NORTH) {
            // For north face: use (1.0 - x) so left is 0 and right is 1
            xz = 1.0 - blockCoord.x;
        } else if (blockFacing == Direction.SOUTH) {
            // For south face: x directly maps (left to right = 0 to 1)
            xz = blockCoord.x;
        } else if (blockFacing == Direction.WEST) {
            // For west face: use (1.0 - z) so left is 0 and right is 1
            xz = blockCoord.z;
        } else { // EAST
            // For east face: z directly maps (left to right = 0 to 1)
            xz = 1.0 - blockCoord.z;
        }

        // Determine which quadrant was hit
        boolean isRightColumn = (xz > ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD);
        boolean isTopSection = y > ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD;
        boolean isBotSection = y < ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD;

        if (isRightColumn) {
            if (y > ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD) {
                //nothing, block position area
            } else if (y > ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD) {
                //Chunk section depth up down
                float diff = ORDINAL_COORD_BLOCK_VERT_TOP_THRESHOLD - ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD;
                cmd = (y > ORDINAL_COORD_BLOCK_VERT_BOT_THRESHOLD + diff / 2) ? 5 : 6;
            } else {
                cmd = 9; //Display height adjust
            }

        } else if (isTopSection) {
            //Nothing, block position area

        } else if (isBotSection) {

            if (xz > ORDINAL_COORD_BLOCK_HORZ_RIGHT_THRESHOLD) {
                cmd = 9; //Display height adjust - prob never reaches
            } else if (xz < ORDINAL_COORD_BLOCK_HORZ_LEFT_THRESHOLD) {
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

            if (y > upThreshold) {
                input = 1; // Top Arrow
            } else if (y < downThreshold) {
                input = 2; // Bottom Arrow
            } else if (xz < leftThreshold) {
                input = 3; // Left Arrow
            } else if (xz > rightThreshold) {
                input = 4; // Right Arrow
            }

            cmd = getDirectionFromArrow(blockFacing, input);


        }

        return cmd;
    }

    /*
     * Up arrow should move OPPOSITE direction the block is facing,
     *  down arrow should move towards, side arrows fill respectively
     */
        static int getDirectionFromArrow(Direction blockFacing, int input)
        {
            int cmd = -1;
            if (blockFacing == Direction.NORTH) {
                if (input == 1) cmd = 2; //Up arrow moves South
                else if (input == 2) cmd = 1; //Down arrow moves North
                else if (input == 3) cmd = 4; //left moves East
                else if (input == 4) cmd = 3; //East moves East
            } else if (blockFacing == Direction.SOUTH) {
                if (input == 1) cmd = 1; //Up arrow moves North
                else if (input == 2) cmd = 2; //Down arrow moves South
                else if (input == 3) cmd = 3; //West moves East
                else if (input == 4) cmd = 4; //East moves West
            } else if (blockFacing == Direction.EAST) {
                if (input == 1) cmd = 3; //Up arrow moves West
                else if (input == 2) cmd = 4; //Down arrow moves East
                else if (input == 3) cmd = 1; //West moves North
                else if (input == 4) cmd = 2; //East moves South
            } else if (blockFacing == Direction.WEST) {
                if (input == 1) cmd = 4; //Up arrow moves East
                else if (input == 2) cmd = 3; //Down arrow moves West
                else if (input == 3) cmd = 2; //West moves South
                else if (input == 4) cmd = 1; //East moves North
            }
            return cmd;
        }


    // Position Controller constants
    float POSITION_MIDDLE_LEFT = 0.2f;
    float POSITION_MIDDLE_RIGHT = 0.8f;
    float POSITION_MIDDLE_TOP = 0.8f;
    float POSITION_MIDDLE_BOTTOM = 0.2f;
    float POSITION_ARROW_SIZE = 0.3f;
    /**
     * Position Controller - Four arrows in middle area to move satellite ordinally
     */
    static int calculateHitCommandPosition(BlockHitResult res)
    {
        int cmd = -1;
        BlockPos p = res.getBlockPos();
        Vec3 blockCoord = res.getLocation().subtract(p.getX(), p.getY(), p.getZ());
        Direction blockFacing = res.getDirection();
        if(blockFacing == Direction.UP || blockFacing == Direction.DOWN) return cmd;

        double xz;
        double y = blockCoord.y;

        // Map coordinates correctly based on facing direction
        if (blockFacing == Direction.NORTH) {
            xz = 1.0 - blockCoord.x;
        } else if (blockFacing == Direction.SOUTH) {
            xz = blockCoord.x;
        } else if (blockFacing == Direction.WEST) {
            xz = blockCoord.z;
        } else { // EAST
            xz = 1.0 - blockCoord.z;
        }

        // Check for middle arrows
        boolean inMiddleX = (xz > (0.5 - POSITION_ARROW_SIZE/2)) && (xz < (0.5 + POSITION_ARROW_SIZE/2));
        boolean inMiddleY = (y > (0.5 - POSITION_ARROW_SIZE/2)) && (y < (0.5 + POSITION_ARROW_SIZE/2));

        int input = -1;
        
        // Top arrow
        if (inMiddleX && y > POSITION_MIDDLE_TOP - POSITION_ARROW_SIZE/2 ) {
            input = 1;
        }
        // Bottom arrow
        else if (inMiddleX && y < POSITION_MIDDLE_BOTTOM + POSITION_ARROW_SIZE/2) {
            input = 2;
        }
        // Left arrow
        else if (inMiddleY  && xz < POSITION_MIDDLE_LEFT + POSITION_ARROW_SIZE/2) {
            input = 3;
        }
        // Right arrow
        else if (inMiddleY && xz > POSITION_MIDDLE_RIGHT - POSITION_ARROW_SIZE/2 ) {
            input = 4;
        }

        if(input > 0)
            cmd = getDirectionFromArrow(blockFacing, input);

        return cmd;
    }

    // Height Controller constants
    float HEIGHT_LEFT_COLUMN = 0.25f;
    float HEIGHT_RIGHT_COLUMN = 0.75f;
    float HEIGHT_COLUMN_WIDTH = 0.2f;
    float HEIGHT_TOP_ARROW = 0.75f;
    float HEIGHT_BOTTOM_ARROW = 0.25f;
    float HEIGHT_ARROW_HEIGHT = 0.15f;

    /**
     * Height Controller - Two columns with arrows, left adjusts section depth (5,6), right adjusts height (7,8)
     */
    static int calculateHitCommandHeight(BlockHitResult res)
    {
        int cmd = -1;
        BlockPos p = res.getBlockPos();
        Vec3 blockCoord = res.getLocation().subtract(p.getX(), p.getY(), p.getZ());
        Direction blockFacing = res.getDirection();
        if(blockFacing == Direction.UP || blockFacing == Direction.DOWN) return cmd;

        double xz;
        double y = blockCoord.y;

        // Map coordinates correctly based on facing direction
        if (blockFacing == Direction.NORTH) {
            xz = 1.0 - blockCoord.x;
        } else if (blockFacing == Direction.SOUTH) {
            xz = blockCoord.x;
        } else if (blockFacing == Direction.WEST) {
            xz = blockCoord.z;
        } else { // EAST
            xz = 1.0 - blockCoord.z;
        }

        // Check left column (section depth adjustment)
        if (xz > (HEIGHT_LEFT_COLUMN - HEIGHT_COLUMN_WIDTH/2) && xz < (HEIGHT_LEFT_COLUMN + HEIGHT_COLUMN_WIDTH/2)) {
            // Top arrow - increase section depth
            if (y > (HEIGHT_TOP_ARROW - HEIGHT_ARROW_HEIGHT/2) && y < (HEIGHT_TOP_ARROW + HEIGHT_ARROW_HEIGHT/2)) {
                cmd = 5;
            }
            // Bottom arrow - decrease section depth
            else if (y > (HEIGHT_BOTTOM_ARROW - HEIGHT_ARROW_HEIGHT/2) && y < (HEIGHT_BOTTOM_ARROW + HEIGHT_ARROW_HEIGHT/2)) {
                cmd = 6;
            }
        }
        // Check right column (height adjustment)
        else if (xz > (HEIGHT_RIGHT_COLUMN - HEIGHT_COLUMN_WIDTH/2) && xz < (HEIGHT_RIGHT_COLUMN + HEIGHT_COLUMN_WIDTH/2)) {
            // Top arrow - increase height
            if (y > (HEIGHT_TOP_ARROW - HEIGHT_ARROW_HEIGHT/2) && y < (HEIGHT_TOP_ARROW + HEIGHT_ARROW_HEIGHT/2)) {
                cmd = 7;
            }
            // Bottom arrow - decrease height
            else if (y > (HEIGHT_BOTTOM_ARROW - HEIGHT_ARROW_HEIGHT/2) && y < (HEIGHT_BOTTOM_ARROW + HEIGHT_ARROW_HEIGHT/2)) {
                cmd = 8;
            }
        }

        return cmd;
    }

    // Target Controller constants
    float TARGET_BOTTOM_ROW = 0.35f;
    float TARGET_LEFT_BUTTON = 0.25f;
    float TARGET_RIGHT_BUTTON = 0.75f;
    float TARGET_BUTTON_WIDTH = 0.2f;

    /**
     * Target Controller - Two buttons on bottom row, left returns 10, right returns 11
     */
    static int calculateHitCommandTarget(BlockHitResult res)
    {
        int cmd = -1;
        BlockPos p = res.getBlockPos();
        Vec3 blockCoord = res.getLocation().subtract(p.getX(), p.getY(), p.getZ());
        Direction blockFacing = res.getDirection();
        if(blockFacing == Direction.UP || blockFacing == Direction.DOWN) return cmd;

        double xz;
        double y = blockCoord.y;

        // Map coordinates correctly based on facing direction
        if (blockFacing == Direction.NORTH) {
            xz = 1.0 - blockCoord.x;
        } else if (blockFacing == Direction.SOUTH) {
            xz = blockCoord.x;
        } else if (blockFacing == Direction.WEST) {
            xz = blockCoord.z;
        } else { // EAST
            xz = 1.0 - blockCoord.z;
        }

        // Check if in bottom row
        if (y < TARGET_BOTTOM_ROW) {
            // Left button
            if (xz > (TARGET_LEFT_BUTTON - TARGET_BUTTON_WIDTH/2) && xz < (TARGET_LEFT_BUTTON + TARGET_BUTTON_WIDTH/2)) {
                cmd = 10;
            }
            // Right button
            else if (xz > (TARGET_RIGHT_BUTTON - TARGET_BUTTON_WIDTH/2) && xz < (TARGET_RIGHT_BUTTON + TARGET_BUTTON_WIDTH/2)) {
                cmd = 11;
            }
            //center wool changer
            else if (xz > (0.5 - TARGET_BUTTON_WIDTH/4) && xz < (0.5 + TARGET_BUTTON_WIDTH/4)) {
                cmd = 16;
            }
        }

        return cmd;
    }


    // Upgrade Controller constants
    float UPGRADE_BOTTOM_ROW = 0.3f;
    float UPGRADE_LEFT_BUTTON = 0.25f;
    float UPGRADE_RIGHT_BUTTON = 0.75f;
    float UPGRADE_BUTTON_WIDTH = 0.2f;
    float UPGRADE_TOP_SECTION = 0.4f;
    float UPGRADE_QUAD_SPLIT = 0.5f;

    /**
     * Upgrade Controller - Bottom row with on/off and color controls, top section with 4 quadrants (12-15)
     */
    static int calculateHitCommandUpgrade(BlockHitResult res)
    {

        int cmd = -1;
        BlockPos p = res.getBlockPos();
        Vec3 blockCoord = res.getLocation().subtract(p.getX(), p.getY(), p.getZ());
        Direction blockFacing = res.getDirection();
        if(blockFacing == Direction.UP || blockFacing == Direction.DOWN) return cmd;

        double xz;
        double y = blockCoord.y;

        // Map coordinates correctly based on facing direction
        if (blockFacing == Direction.NORTH) {
            xz = 1.0 - blockCoord.x;
        } else if (blockFacing == Direction.SOUTH) {
            xz = blockCoord.x;
        } else if (blockFacing == Direction.WEST) {
            xz = blockCoord.z;
        } else { // EAST
            xz = 1.0 - blockCoord.z;
        }

        // Check bottom row
        if (y < UPGRADE_BOTTOM_ROW) {
            // Left button - on/off toggle
            if (xz > (UPGRADE_LEFT_BUTTON - UPGRADE_BUTTON_WIDTH/2) && xz < (UPGRADE_LEFT_BUTTON + UPGRADE_BUTTON_WIDTH/2)) {
                cmd = 0; // Toggle on/off
            }
            // Right button - wool color
            else if (xz > (UPGRADE_RIGHT_BUTTON - UPGRADE_BUTTON_WIDTH/2) && xz < (UPGRADE_RIGHT_BUTTON + UPGRADE_BUTTON_WIDTH/2)) {
                cmd = 16; // Change wool color
            }
        }
        // Check top section (4 quadrants)
        // 0 1      12 13
        // 2 3      14 14
        else if (y > UPGRADE_TOP_SECTION) {
            // Top-left quadrant
            if (xz < UPGRADE_QUAD_SPLIT && y > (UPGRADE_TOP_SECTION + (1.0 - UPGRADE_TOP_SECTION) * UPGRADE_QUAD_SPLIT)) {
                cmd = 12;
            }
            // Top-right quadrant
            else if (xz > UPGRADE_QUAD_SPLIT && y > (UPGRADE_TOP_SECTION + (1.0 - UPGRADE_TOP_SECTION) * UPGRADE_QUAD_SPLIT)) {
                cmd = 13;
            }
            // Bottom-left quadrant
            else if (xz < UPGRADE_QUAD_SPLIT && y < (UPGRADE_TOP_SECTION + (1.0 - UPGRADE_TOP_SECTION) * UPGRADE_QUAD_SPLIT)) {
                cmd = 14;
            }
            // Bottom-right quadrant
            else if (xz > UPGRADE_QUAD_SPLIT && y < (UPGRADE_TOP_SECTION + (1.0 - UPGRADE_TOP_SECTION) * UPGRADE_QUAD_SPLIT)) {
                cmd = 15;
            }
        }

        return cmd;
    }

}
