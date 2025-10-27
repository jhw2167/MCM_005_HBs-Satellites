package com.holybuckets.satellite.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoloBaseBlock extends Block {

    public static final BlockBehaviour.Properties HOLO_BASE_PROPERTIES = Properties.copy(Blocks.IRON_BLOCK);
//        BlockBehaviour.Properties.copy(Blocks.WATER)
//        .isViewBlocking((state, level, pos) -> false)
//        .isSuffocating((state, level, pos) -> false)
//        .destroyTime(0.1f)
//        .sound(Blocks.WHITE_WOOL.getSoundType(null));

    public static final BlockBehaviour.Properties HOLO_AIR_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.AIR)
        .noOcclusion()
        .isViewBlocking((state, level, pos) -> false)
        .isSuffocating((state, level, pos) -> false)
        .noCollission();

    public HoloBaseBlock(Properties properties) {
        super(properties);
    }


}
