package com.holybuckets.satellite.core;


import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashSet;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;
import static com.holybuckets.satellite.api.ChiselBitsAPI.IGNORE;

public class ChunkDisplayInfo {

    public int levelSectionIndex;
    public LevelChunk chunk;
    public TripleInt blockOffset;
    public int[] holoBits;

    public ChunkDisplayInfo(LevelChunk chunk, int levelSectionIndex) {
        this(levelSectionIndex, chunk, new TripleInt(0, 0, 0));
    }

    public ChunkDisplayInfo(int[] bits) {
        holoBits = bits;
    }

    public ChunkDisplayInfo(int levelSectionIndex, LevelChunk chunk, TripleInt blockOffset) {
        this.levelSectionIndex = levelSectionIndex;
        this.chunk = chunk;
        this.blockOffset = blockOffset;

        holoBits = new int[4096];
        updateBits(holoBits, this, true);
    }

    static Set<Integer> updateBits(int[] cache, ChunkDisplayInfo info, boolean init) {
        LevelChunk chunk = info.chunk;
        LevelChunkSection section = chunk.getSections()[info.levelSectionIndex];
        Set<Integer> changed = new HashSet<>(64);
        // Process each position in the chunk section
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockState originalBlock = section.getBlockState(x, y, z);
                    int p = ISatelliteDisplayBlock.getCachePos(x, y, z);
                    int temp;
                    if (IGNORE.contains(originalBlock.getBlock())) {
                        temp = 0;
                        continue; // Skip ignored blocks
                    }
                    temp = 2;
                    if(!init && cache[p]!=temp) changed.add(p);
                    cache[p] = temp;
                }
            }
        }
        return changed;
    }



    public void delta(int dx, int dy, int dz) {
        blockOffset.x += dx;
        blockOffset.y += dy;
        blockOffset.z += dz;
    }




}
