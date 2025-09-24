package com.holybuckets.satellite.core;


import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashSet;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;
import static com.holybuckets.satellite.api.ChiselBitsAPI.DARK;
import static com.holybuckets.satellite.api.ChiselBitsAPI.IGNORE;

public class ChunkDisplayInfo {

    public int levelSectionIndex;
    public LevelChunk chunk;
    public TripleInt blockOffset;
    public int[] holoBits;
    public boolean[] hasUpdates;

    public int currentYIndexForBatch;
    public boolean isActive;
    public int lifetime;

    public ChunkDisplayInfo(int[] bits) {
        holoBits = bits;
        this.currentYIndexForBatch = -1;
        this.isActive = false;

        this.hasUpdates = new boolean[16];
    }

    public ChunkDisplayInfo(int levelSectionIndex, LevelChunk chunk, TripleInt blockOffset) {
        this.levelSectionIndex = levelSectionIndex;
        this.chunk = chunk;
        this.blockOffset = blockOffset;
        this.currentYIndexForBatch = -1;
        this.isActive = false;

        holoBits = new int[4096];
        hasUpdates = new boolean[16];
        updateBits(holoBits, this, true);
    }

    public ChunkDisplayInfo(LevelChunk chunk, int levelSectionIndex) {
        this(levelSectionIndex, chunk, new TripleInt(0, 0, 0));
    }

    public int augmentBatch() {
        currentYIndexForBatch++;
        if(currentYIndexForBatch > 15) currentYIndexForBatch = 0;
        return currentYIndexForBatch;
    }

    public void resetUpdates() {
        for(int i=0; i<16; i++) hasUpdates[i] = true;
        lifetime = 0;
    }

    public void refreshBits() {
        updateBits(holoBits, this, false);
        lifetime = 0;
    }

    static Set<Integer> updateBits(int[] holoBits, ChunkDisplayInfo info, boolean init) {
        LevelChunk chunk = info.chunk;
        LevelChunkSection section = chunk.getSections()[info.levelSectionIndex];
        Set<Integer> changed = new HashSet<>(64);
        // Process each position in the chunk section
        for (int y = 0; y < 16; y++) {
            info.hasUpdates[y] = false;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockState originalBlock = section.getBlockState(x, y, z);
                    int p = ISatelliteDisplayBlock.getCachePos(x, y, z);
                    int temp;
                    if (IGNORE.contains(originalBlock.getBlock())) {
                        temp = 0;
                    } else if( DARK.contains(originalBlock.getBlock()) ) {
                        temp = 4;
                        info.hasUpdates[y] = true;
                    } else {
                        temp = 2;
                        info.hasUpdates[y] = true;
                    }

                    //if(!init && holoBits[p]!=temp) changed.add(p);
                    holoBits[p] = temp;
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
