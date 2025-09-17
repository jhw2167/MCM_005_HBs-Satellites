package com.holybuckets.satellite.core;

import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkDisplayInfo {

    int levelSectionIndex;
    LevelChunk chunk;

    public ChunkDisplayInfo(int levelSectionIndex, LevelChunk chunk) {
        this.levelSectionIndex = levelSectionIndex;
        this.chunk = chunk;
    }
}
