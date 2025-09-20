package com.holybuckets.satellite.api;

import com.holybuckets.satellite.core.ChunkDisplayInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;

public interface ChiselBitsAPI {

    Set<Block> IGNORE = Set.of(
        Blocks.WATER,
        Blocks.AIR,
        Blocks.LAVA,
        Blocks.SNOW
    );

    static Block HOLO_LIGHT() { return Blocks.WHITE_STAINED_GLASS; }
    static Block HOLO_BASE() { return Blocks.LIGHT_BLUE_STAINED_GLASS; }
    static Block HOLO_DARK() { return  Blocks.BLUE_STAINED_GLASS; }
    static Block HOLO_BLACK() { return  Blocks.BLACK_STAINED_GLASS; }


    public BlockEntity build(Level level, int[] bits, BlockPos pos);

    void clear(Level level, BlockPos pos);

    public void update(ChunkDisplayInfo info, int[] bits, List<TripleInt> updates, BlockPos pos);

    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, TripleInt offset, BlockPos pos);

}
