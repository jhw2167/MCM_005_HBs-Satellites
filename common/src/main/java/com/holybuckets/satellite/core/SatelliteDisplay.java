package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.*;

import static com.holybuckets.foundation.HBUtil.TripleInt;

/**
 * Each Satellite Controller Block contains a single satellite display
 * which contains all Satellite Display Blocks related to this controller
 */
public class SatelliteDisplay {

    private static Map<TripleInt, ChunkDisplayInfo> INFO_CACHE = new LinkedHashMap<>();

    Level level;
    SatelliteBlockEntity satellite;
    SatelliteControllerBlockEntity controller;
    TripleInt offset;
    ChunkPos target;
    int currentSection;
    int depth;
    int lifetime;
    Map<BlockPos, ISatelliteDisplayBlock> displayBlocks;

    public SatelliteDisplay(Level level, SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller) {
        this.level = level;
        this.satellite = satellite;
        this.controller = controller;
        displayBlocks = new HashMap<>();
        if( noSource() ) return;

        this.target = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );

        LevelChunkSection[] sections =  level.getChunk(target.x, target.z).getSections();
        for (int i = sections.length - 1; i >= 1; i--) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            this.currentSection = i;
            break;
        }
        this.depth = 1;
        this.lifetime = 0;
    }

    public void tick() {
        if (!isActive()) {
            lifetime++;
        }
    }

    private boolean isActive() {
        return displayBlocks.values().stream()
                .anyMatch(block -> block.getDisplayInfo() != null && block.getDisplayInfo().isActive);
    }

    public boolean noSource() { return satellite == null; }

    /** delta height
     * @return
     */
    public void setDepth(int delta) {
         int temp = this.depth + delta;
         if(temp < 1 || temp > 4) return;
        this.depth += delta;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setCurrentSection(int section) {
        this.currentSection = section;
    }

    public void add(BlockPos blockPos, ISatelliteDisplayBlock displayBlock) {
        displayBlocks.put(blockPos, displayBlock);
    }

    public void addAll(Map<BlockPos, ISatelliteDisplayBlock> blocks) {
        displayBlocks.putAll(blocks);
    }

    public void remove(BlockPos blockPos) {
        displayBlocks.remove(blockPos);
    }

    public boolean contains(BlockPos blockPos) {
        return displayBlocks.containsKey(blockPos);
    }

    public void clear() {
        displayBlocks.clear();
    }

    public Deque<ChunkDisplayInfo> initDisplayInfo(SatelliteDisplayBlockEntity displayblock) {

        Deque<ChunkDisplayInfo> infoList = new ArrayDeque<>();
        for(int i = currentSection; i > currentSection - depth; i--) {
            if(i < 0) break;
            ChunkDisplayInfo info = getDisplayInfo(displayblock, i);
            if(info == null) break;
            infoList.push( info );
        }

        return infoList;
    }

    public ChunkDisplayInfo getDisplayInfo(SatelliteDisplayBlockEntity displayblock, int section) {
        BlockPos blockPos = displayblock.getBlockPos();
        int xDiff = blockPos.getX() - controller.getBlockPos().getX();
        int zDiff = blockPos.getZ() - controller.getBlockPos().getZ();

        TripleInt chunkSelection = new TripleInt(target.x + xDiff, section, target.z + zDiff);
        LevelChunk chunk = level.getChunk(chunkSelection.x, chunkSelection.z);
        if(chunk.getSections().length <= section) return null;

        ChunkDisplayInfo info = INFO_CACHE.putIfAbsent(chunkSelection, new ChunkDisplayInfo(chunk, section));
        if(info != null) {
            if(info.isActive)
                info.resetUpdates();
            else
                info.refreshBits();
            info.isActive = true;
        }
        return INFO_CACHE.get(chunkSelection);

    }
}
