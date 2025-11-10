package com.holybuckets.satellite.externalapi;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.HoloBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.holybuckets.satellite.config.ModConfig.MAX_ORE_SCANNER_MAPPINGS;
import static net.minecraft.world.level.block.Blocks.AIR;

public class ChiselBitsAPIForge implements ChiselBitsAPI {

    static final IBlockInformation[] HOLO_BLOCKS = new IBlockInformation[64+ChiselBitsAPI.OREMAP_START_IDX];

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

        //Fill with ore maps
        final int MAX = MAX_ORE_SCANNER_MAPPINGS + ChiselBitsAPI.OREMAP_START_IDX;
        for(int i = ChiselBitsAPI.OREMAP_START_IDX; i < MAX; i++) {
            Block HOLO_ORE = ChiselBitsAPI.HOLO_ORE_BLOCK(i);
            if(HOLO_ORE == null) break;
            HOLO_BLOCKS[i] = new BlockInformation( HOLO_ORE.defaultBlockState(),
                IStateVariantManager.getInstance().getStateVariant(HOLO_ORE.defaultBlockState(), CHISELED)
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
        try(IBatchMutation m = mutator.batch() )
        {
            AtomicInteger i = new AtomicInteger(0);
                for(int y = 0; y < 16; y++) {
                    if(yLevelHasUpdates != null && !yLevelHasUpdates[y]) continue;
                    for(int x = 0; x < 16; x++) {
                        for(int z = 0; z < 16; z++) {
                            if(bits[ISatelliteDisplayBE.getCachePos(x,y,z)] == 0 ) {
                                mutator.clearInAreaTarget( ISatelliteDisplayBE.get3Dpos(x,y,z) );
                                continue;
                            }
                            mutator.overrideInAreaTarget(
                                HOLO_BLOCKS[ bits[ISatelliteDisplayBE.getCachePos(x,y,z)] ],
                                ISatelliteDisplayBE.get3Dpos(x,y,z)
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
                        mutator.clearInAreaTarget( ISatelliteDisplayBE.get3Dpos(x,yLevel,z) );
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



    @Override
    public boolean isChiseledBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof ChiseledBlock)) return false;
        BlockEntity be = level.getBlockEntity(pos);
        return (be instanceof IMultiStateBlockEntity);
    }

    @Override
    public boolean isViewingHoloBit(Level level, BlockHitResult hitResult, Vec3 offset) {

        Vec3 target = hitResult.getLocation().add(offset);
        BlockPos blockPos = new BlockPos(
            (int) Math.floor(target.x),
            (int) Math.floor(target.y),
            (int) Math.floor(target.z)
        );
        return isViewingHoloBlock(level, blockPos, target);
    }

    @Override
    public boolean isViewingHoloBlock(Level level, BlockPos pos, Vec3 loc) {

        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof ChiseledBlock)) return false;
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof IMultiStateBlockEntity cbe)) return false;
        Vec3 target = ChiselBitsAPI.clamp(loc, pos );
        if(!(cbe.isInside(target))) return false;
        BlockState internalState = cbe.getInAreaTarget(target)
            .get().getBlockInformation().getBlockState();

        return (internalState.getBlock() instanceof HoloBlock);
    }

    @Override
    public boolean isViewingHoloBlock(Level level, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Vec3 loc = hitResult.getLocation();
        return isViewingHoloBlock(level, pos, loc);
    }


    //END BUILD



}
