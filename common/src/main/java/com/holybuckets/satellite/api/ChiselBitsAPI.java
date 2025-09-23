package com.holybuckets.satellite.api;

import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;

public interface ChiselBitsAPI {

    Set<Block> IGNORE = new HashSet<>();

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

        //1. Add all liquid blocks
        event.getServer().registryAccess().registryOrThrow(Registries.BLOCK)
            .forEach( block -> {

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
                }
            });

        //2. Add all leaf blocks, seweed, shruberry, grasss, flowers, etc

    }

    static Block HOLO_LIGHT() { return Blocks.WHITE_STAINED_GLASS; }
    static Block HOLO_BASE() { return Blocks.LIGHT_BLUE_STAINED_GLASS; }
    static Block HOLO_DARK() { return  Blocks.BLUE_STAINED_GLASS; }
    static Block HOLO_BLACK() { return  Blocks.BLACK_STAINED_GLASS; }


    public BlockEntity build(Level level, int[] bits, BlockPos pos);

    void clear(Level level, BlockPos pos);

    public void update(ChunkDisplayInfo info, int[] bits, List<TripleInt> updates, BlockPos pos);

    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, TripleInt offset, BlockPos pos);

}
