package com.holybuckets.satellite.externalapi;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.HoloBaseBlock;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.IBatchMutation;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.multistate.mutator.ChiselAdaptingWorldMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.Blocks.AIR;

public class ChiselBitsAPIForge implements ChiselBitsAPI {

    static final IBlockInformation[] HOLO_BLOCKS = new IBlockInformation[16];

    private static void initHolo(Level level)
    {
        if(HOLO_BLOCKS[0] != null) return;

        //GetBlock Entity from registry
        /*
        ChiseledBlockEntity CHISELED = level.registryAccess()
            .registryOrThrow(Registries.BLOCK_ENTITY_TYPE)
            .get(new ResourceLocation(Constants.MOD_ID_CHISELED_BITS, "chiseled_block")).
        */
        Optional<BlockEntity> CHISELED = Optional.empty();
        BlockState AIR = ChiselBitsAPI.HOLO_EMPTY().defaultBlockState();
        HOLO_BLOCKS[0] = new BlockInformation( AIR, IStateVariantManager.getInstance()
            .getStateVariant(AIR, CHISELED)
        );

        BlockState LIGHT = ChiselBitsAPI.HOLO_LIGHT().defaultBlockState();
        HOLO_BLOCKS[1] = new BlockInformation( LIGHT,
            IStateVariantManager.getInstance().getStateVariant(LIGHT, Optional.empty())
        );

        BlockState BASE = ChiselBitsAPI.HOLO_BASE().defaultBlockState();
        HOLO_BLOCKS[2] = new BlockInformation( BASE,
            IStateVariantManager.getInstance().getStateVariant(BASE, CHISELED)
        );

        BlockState DARK = ChiselBitsAPI.HOLO_DARK().defaultBlockState();
        HOLO_BLOCKS[3] = new BlockInformation( DARK,
            IStateVariantManager.getInstance().getStateVariant(DARK, CHISELED)
        );

        BlockState BLACK = ChiselBitsAPI.HOLO_BLACK().defaultBlockState();
        HOLO_BLOCKS[4] = new BlockInformation( BLACK,
            IStateVariantManager.getInstance().getStateVariant(BLACK, CHISELED)
        );

        //Fill with [4] up to index 6
        for(int i = 5; i < ChiselBitsAPI.DEMARCATOR_START_IDX; i++) HOLO_BLOCKS[i] = HOLO_BLOCKS[4];

        //Fill with stained glass colors up to 15
        for(int i = ChiselBitsAPI.DEMARCATOR_START_IDX; i < 16; i++) {
            BlockState STAINED = ChiselBitsAPI.DEMARCATOR(i-8).defaultBlockState();
            HOLO_BLOCKS[i] = new BlockInformation( STAINED,
                IStateVariantManager.getInstance().getStateVariant(STAINED, CHISELED)
            );
        }
    }

    @Override
    public BlockEntity build(Level level, int[] bits, BlockPos pos) {
        return build(level, bits, pos, null);
    }

    @Override
    public BlockEntity build(Level level, int[] bits, BlockPos pos, boolean[] yLevelHasUpdates)
    {
        initHolo(level);
        BlockState above = level.getBlockState(pos);
        BlockEntity aboveBe = level.getBlockEntity(pos);
        boolean aboveIsAir = above.equals(AIR.defaultBlockState());
        boolean aboveIsChiseled = aboveBe instanceof IMultiStateBlockEntity;
        if(!aboveIsAir && !aboveIsChiseled) { return null; }

        ChiselAdaptingWorldMutator mutator = new ChiselAdaptingWorldMutator(level, pos);
        try(IBatchMutation m = mutator.batch() ) {
                for(int y = 0; y < 16; y++) {
                    if(yLevelHasUpdates != null && !yLevelHasUpdates[y]) continue;
                    for(int x = 0; x < 16; x++) {
                        for(int z = 0; z < 16; z++) {
                            mutator.overrideInAreaTarget(
                                HOLO_BLOCKS[ bits[ISatelliteDisplayBlock.getCachePos(x,y,z)] ],
                                ISatelliteDisplayBlock.get3Dpos(x,y,z)
                            );
                        }
                    }
                    if(yLevelHasUpdates != null) yLevelHasUpdates[y] = false;
                }
        } catch (Exception e) {
            LoggerProject.logError ("100003","Error placing chiseled block at " + pos + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        //return  blockEntity;
        return level.getBlockEntity(pos);
    }
    @Override
    public void clear(Level level, BlockPos pos) {
        clear(level, pos, -1);
    }

    @Override
    public void clear(Level level, BlockPos pos, int yLevel)
    {

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if( yLevel < 0 || yLevel > 15) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
        } else if(!(blockEntity instanceof ChiseledBlockEntity)) {
                level.setBlock(pos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
                level.removeBlockEntity(pos);
        } else {
            ChiselAdaptingWorldMutator mutator = new ChiselAdaptingWorldMutator(level, pos);
            try(IBatchMutation m = mutator.batch() ) {
                for(int x = 0; x < 16; x++) {
                    for(int z = 0; z < 16; z++) {
                        mutator.clearInAreaTarget( ISatelliteDisplayBlock.get3Dpos(x,yLevel,z) );
                    }
                }
            } catch (Exception e) {
            }
        }

    }

    @Override
    public void update(ChunkDisplayInfo info, int[] bits, List<HBUtil.TripleInt> updates, BlockPos pos) {

    }

    @Override
    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, HBUtil.TripleInt offset, BlockPos pos) {

    }


    /**
     * Update the area in the blockPos and area specified
     *
     * @param level
     * @param pos
     * @param area
     * @param colors
     */
    @Override
    public void highlightArea(Level level, BlockPos center, Vec3[] area, int[] colors)
    {

        //Generates positions in multiple different chiseled blocks
        // to do later maybe
        //ChiselAdaptingWorldMutator mutator = new ChiselAdaptingWorldMutator(level, pos);
        ChiselAdaptingWorldMutator[][][] mutatorArr = new ChiselAdaptingWorldMutator[3][3][3];
        //generator a mutator entry for each block around the center, if its a holo block
        for(int y = -1; y <= 1; y++) {
            for(int x = -1; x <= 1; x++) {
                for(int z = -1; z <= 1; z++) {
                    BlockPos pos = center.offset(x,y,z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if(be != null && be instanceof IMultiStateBlockEntity) {
                        mutatorArr[y+1][z+1][z+1] = new ChiselAdaptingWorldMutator(level, pos);
                    }
                }
            }
        }

        final HBUtil.TripleInt centerIdx = new HBUtil.TripleInt(1,1,1);
        HBUtil.TripleInt idx = new HBUtil.TripleInt(1,1,1);
        try {
            for(int i = 0; i < area.length; i++)
            {
                idx.x = centerIdx.x;
                idx.y = centerIdx.y;
                idx.z = centerIdx.z;
                int dx=0, dy=0, dz=0;


                if(area[i].x < 0) {idx.x = 0; dx=1;}
                else if(area[i].x >= 1) {idx.x = 2; dx=-1;}
                if(area[i].y < 0) {idx.y = 0; dy=1;}
                else if(area[i].y >= 1) {idx.y = 2; dy=-1;}
                if(area[i].z < 0) {idx.z = 0; dz=1;}
                else if(area[i].z >= 1) {idx.z = 2; dz=-1;}

                ChiselAdaptingWorldMutator mutator = mutatorArr[idx.y][idx.x][idx.z];
                if(mutator == null) continue;
                Vec3 areaPrime = area[i].add(dx,dy,dz);

                if(!( mutator.getInAreaTarget( areaPrime )).isPresent()) continue;
                BlockState curr = mutator.getInAreaTarget( areaPrime ).get().getBlockInformation().getBlockState();
                if(curr == null || curr.getBlock() == AIR || curr.getBlock() == ModBlocks.holoAirBlock) continue;
                mutator.overrideInAreaTarget( HOLO_BLOCKS[ colors[i] ],  areaPrime );
            }
        } catch (Exception e) {
            LoggerProject.logError ("100003","Error placing chiseled block at " + center + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isViewingHoloBlock(Level level, BlockHitResult hitResult) {

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof ChiseledBlock)) return false;
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof IMultiStateBlockEntity cbe)) return false;
        Vec3 target = ChiselBitsAPI.clamp(hitResult.getLocation(), pos );
        if(!(cbe.isInside(target))) return false;
        BlockState internalState = cbe.getInAreaTarget(target)
            .get().getBlockInformation().getBlockState();

        return (internalState.getBlock() instanceof HoloBaseBlock);
    }


    //END BUILD

}
