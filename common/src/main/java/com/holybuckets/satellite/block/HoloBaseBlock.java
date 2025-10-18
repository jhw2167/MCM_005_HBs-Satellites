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

    public static final BlockBehaviour.Properties HOLO_BASE_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.GLASS)
        .noOcclusion()
        .isViewBlocking((state, level, pos) -> false)
        .isSuffocating((state, level, pos) -> false)
        .destroyTime(0.1f)
        .sound(Blocks.WHITE_WOOL.getSoundType(null))
        .noCollission();

    public static final BlockBehaviour.Properties HOLO_AIR_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.AIR)
        .noOcclusion()
        .isViewBlocking((state, level, pos) -> false)
        .isSuffocating((state, level, pos) -> false)
        .noCollission();

    public HoloBaseBlock(Properties properties) {
        super(properties);
    }


    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }


    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
}
