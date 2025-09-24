package com.holybuckets.satellite.api;

import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;

public interface ChiselBitsAPI {

    Set<Block> IGNORE = new HashSet<>();

    Set<Block> DARK = new HashSet<>();

    static void init(EventRegistrar reg) {
        reg.registerOnBeforeServerStarted(ChiselBitsAPI::onServerStart);
    }

    static void onServerStart(ServerStartingEvent event) {

        //Finish initializing ignoreBlocks

        //Explicit ignores
        IGNORE.addAll( Set.of(
            Blocks.WATER,
            Blocks.AIR,
            Blocks.LAVA,
            Blocks.SNOW
        ));


        event.getServer().registryAccess().registryOrThrow(Registries.BLOCK)
            .forEach( block -> {

            //** 1. START IGNORE BLOCKS

                VoxelShape shape = block.defaultBlockState().getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
                if (!shape.equals(Shapes.block())) {
                    IGNORE.add(block);
                    return;
                }

                RenderShape renderShape = block.defaultBlockState().getRenderShape();
                if (renderShape != RenderShape.MODEL) {
                    IGNORE.add(block); // This block uses custom rendering or is invisible
                    return;
                }

                //check if block or state can be waterlogged
                if(block.getStateDefinition().getProperty("waterlogged") != null) {
                    IGNORE.add(block);
                    return;
                }

                if(block.defaultBlockState().getFluidState().isSource()) {
                    IGNORE.add(block);
                    return;
                }
                //** END IGNORE BLOCKS

                //** 2. START ADD DARK BLOCKS

                BlockState state = block.defaultBlockState();

                //Only add solid blocks that are mineable with pickaxe
                if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                    DARK.add(block);
                }

                //** END ADD DARK BLOCKS


            });

        //2. Add all leaf blocks, seweed, shruberry, grasss, flowers, etc

    }

    static Block HOLO_LIGHT() { return Blocks.WHITE_STAINED_GLASS; }
    static Block HOLO_BASE() {
        //return Blocks.WATER;
        return Blocks.LIGHT_BLUE_STAINED_GLASS;
    }
    static Block HOLO_DARK() { return  Blocks.CLAY ; }
    static Block HOLO_BLACK() { return  Blocks.STONE; }


    public BlockEntity build(Level level, int[] bits, BlockPos pos);

    public BlockEntity build(Level level, int[] bits, BlockPos pos, boolean[] doRenderYLevel);

    void clear(Level level, BlockPos pos);

    void clear(Level level, BlockPos pos, int yLevel);

    public void update(ChunkDisplayInfo info, int[] bits, List<TripleInt> updates, BlockPos pos);

    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, TripleInt offset, BlockPos pos);

}
