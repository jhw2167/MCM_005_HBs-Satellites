package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.*;

/**
 * Each Satellite Controller Block contains a single satellite display
 * which contains all Satellite Display Blocks related to this controller
 */
public class SatelliteDisplay {

    Level level;
    SatelliteBlockEntity satellite;
    SatelliteControllerBlockEntity controller;
    HBUtil.TripleInt offset;
    ChunkPos target;
    int currentSection;
    int height;
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
            this.currentSection = i-1;
            break;
        }
        this.height = 1;

    }

    public boolean noSource() { return satellite == null; }

    public void setHeight(int height) {
        this.height = height;
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
        for(int i = currentSection; i < currentSection + height; i++) {
            ChunkDisplayInfo info = getDisplayInfo(displayblock, i);
            if(info == null) break;
            infoList.add( info );
        }

        return infoList;
    }

    public ChunkDisplayInfo getDisplayInfo(SatelliteDisplayBlockEntity displayblock, int section) {
        BlockPos blockPos = displayblock.getBlockPos();
        int xDiff = blockPos.getX() - controller.getBlockPos().getX();
        int zDiff = blockPos.getZ() - controller.getBlockPos().getZ();

        LevelChunk chunk = level.getChunk(target.x + xDiff , target.z + zDiff);
        if(chunk.getSections().length <= section) return null;
        return new ChunkDisplayInfo(chunk, section);
    }
}
