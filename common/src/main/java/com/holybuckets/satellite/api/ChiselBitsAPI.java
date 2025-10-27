package com.holybuckets.satellite.api;

import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;

public interface ChiselBitsAPI {

    int DEMARCATOR_START_IDX = 8;

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

                //add all wood logs
                if (state.is(BlockTags.LOGS)) {
                    DARK.add(block);
                }

                //** END ADD DARK BLOCKS

            });

        //2. Add all leaf blocks, seweed, shruberry, grasss, flowers, etc
        //Ignore vine
        IGNORE.add(Blocks.VINE);

    }

    static Block HOLO_EMPTY() {
        //return ModBlocks.holoAirBlock;
        return Blocks.AIR;
    }
    static Block HOLO_LIGHT() { return Blocks.WHITE_STAINED_GLASS; }
    static Block HOLO_BASE() {
        return ModBlocks.holoBaseBlock;
    }
    static Block HOLO_DARK() { return  ModBlocks.holoDarkBlock ; }
    static Block HOLO_BLACK() { return  ModBlocks.holoDarkBlock; }

    static List<Player> players = new ArrayList<>();
    static int DEMARCATOR(Player p) {
        int color = 0;
        if(p != null) {
            color = players.indexOf(p);
            if(color < 0) {
                players.add(p);
                color = players.size() - 1;
            }
            color = (color % DEMARCATOR_START_IDX);
        }
        return color;
    }

    static Block DEMARCATOR(int i) {
        //Up to 9 stained glass
        if(i < 0 || i > 8) i = 0;
        switch(i) {
            case 0: return Blocks.ORANGE_STAINED_GLASS;
            case 1: return Blocks.YELLOW_STAINED_GLASS;
            case 2: return Blocks.LIME_STAINED_GLASS;
            case 3: return Blocks.GREEN_STAINED_GLASS;
            default: return Blocks.ORANGE_STAINED_GLASS;

        }
    }

    public BlockEntity build(Level level, int[] bits, BlockPos pos);

    public BlockEntity build(Level level, int[] bits, BlockPos pos, boolean[] doRenderYLevel);

    void clear(Level level, BlockPos pos);

    void clear(Level level, BlockPos pos, int yLevel);

    public void update(ChunkDisplayInfo info, int[] bits, List<TripleInt> updates, BlockPos pos);

    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, TripleInt offset, BlockPos pos);


    boolean isChiseledBlock(Level level, BlockPos pos);

    boolean isViewingHoloBit(Level level, BlockHitResult hitResult, Vec3 offset);

    boolean isViewingHoloBlock(Level level, BlockPos pos, Vec3 loc);

    public boolean isViewingHoloBlock(Level level, BlockHitResult hitResult);

    static final double EPSILON = 0.0001;
    static Vec3 clamp(Vec3 hitLoc, BlockPos pos) {
        Vec3 target = hitLoc.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3 center = pos.getCenter();
        // Clamp to (EPSILON, 1.0 - EPSILON) range - handles all boundaries
        double x = Math.max(EPSILON, Math.min(1.0 - EPSILON, target.x));
        double y = Math.max(EPSILON, Math.min(1.0 - EPSILON, target.y));
        double z = Math.max(EPSILON, Math.min(1.0 - EPSILON, target.z));

        // Move point closer to center by EPSILON
        // If x > center.x, subtract EPSILON (move left toward center)
        // If x < center.x, add EPSILON (move right toward center)
        x += (center.x > x) ? EPSILON : -EPSILON;
        y += (center.y > y) ? EPSILON : -EPSILON;
        z += (center.z > z) ? EPSILON : -EPSILON;

        // Ensure we didn't push outside bounds after adjustment
        x = Math.max(EPSILON, Math.min(1.0 - EPSILON, x));
        y = Math.max(EPSILON, Math.min(1.0 - EPSILON, y));
        z = Math.max(EPSILON, Math.min(1.0 - EPSILON, z));

        return new Vec3(x, y, z);
    }

}
