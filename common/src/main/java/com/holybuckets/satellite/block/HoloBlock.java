package com.holybuckets.satellite.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoloBlock extends HoloBaseBlock {

    public static final Properties HOLO_BASE_PROPERTIES = Properties.copy(Blocks.GLASS)
        .noOcclusion()
        .isViewBlocking((state, level, pos) -> false)
        .isSuffocating((state, level, pos) -> false)
        .destroyTime(0.1f)
        .sound(Blocks.WHITE_WOOL.getSoundType(null))
        .noCollission();

    public static final Properties HOLO_AIR_PROPERTIES = Properties.copy(Blocks.AIR)
        .noOcclusion()
        .isViewBlocking((state, level, pos) -> false)
        .isSuffocating((state, level, pos) -> false)
        .noCollission();

    public HoloBlock(Properties properties) {
        super(properties);
    }

}
