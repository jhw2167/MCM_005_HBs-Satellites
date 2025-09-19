package com.holybuckets.satellite.externalapi;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.client.model.data.ChiseledBlockModelDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class ChiselBitsAPIForge implements ChiselBitsAPI {

    private static final IBlockInformation[] HOLO_BLOCKS = new IBlockInformation[8];

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
        BlockState AIR = Blocks.AIR.defaultBlockState();
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
    }

    @Override
    public BlockEntity build(Level level, int[] bits, BlockPos pos)
    {
        initHolo(level);

        ResourceLocation blockId = new ResourceLocation(Constants.MOD_ID_CHISELED_BITS, "chiseled");
        Block chiseledBlock = level.registryAccess()
            .registryOrThrow(Registries.BLOCK)
            .get(blockId);

        if (chiseledBlock == null) return null;

        level.setBlock(pos, chiseledBlock.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof  ChiseledBlockEntity)) return blockEntity;
        if(!level.isClientSide) return blockEntity;

        // Fill all bits with HOLO_BASE using batch mutation for performance
        try {
            // Get the storage field via reflection
            Field storageField = blockEntity.getClass().getDeclaredField("storage");
            storageField.setAccessible(true);
            IStateEntryStorage storage = (IStateEntryStorage) storageField.get(blockEntity);

            if (storage == null) {
                System.err.println("Storage field is null");
                return blockEntity;
            }
            //iterate over all bits, and setBlockInfo
            for(int y = 0; y < 16; y++) {
                for(int x = 0; x < 16; x++) {
                    for(int z = 0; z < 16; z++) {
                       storage.setBlockInformation(x, y, z,
                        HOLO_BLOCKS[ bits[ISatelliteDisplayBlock.getCachePos(x,y,z)] ] );
                    }
                }
            }

        } catch (NoSuchFieldException e) {
            System.err.println("Could not find 'storage' field: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Could not access 'storage' field: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error manipulating storage: " + e.getMessage());
            e.printStackTrace();
        }

        ChiseledBlockModelDataManager.getInstance().updateModelData((ChiseledBlockEntity) blockEntity);
        return blockEntity;
    }

    @Override
    public void update(ChunkDisplayInfo info, int[] bits, List<HBUtil.TripleInt> updates, BlockPos pos) {

    }

    @Override
    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, HBUtil.TripleInt offset, BlockPos pos) {

    }
    //END BUILD

}
