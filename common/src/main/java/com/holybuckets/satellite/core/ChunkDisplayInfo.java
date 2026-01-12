package com.holybuckets.satellite.core;


import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.config.ModConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.TripleInt;
import static com.holybuckets.satellite.api.ChiselBitsAPI.DARK;
import static com.holybuckets.satellite.api.ChiselBitsAPI.IGNORE;

public class ChunkDisplayInfo {

    public int levelSectionIndex;
    public LevelChunk chunk;
    public TripleInt blockOffset;
    public int[] holoBits;
    public int[] oreScanBits;
    public boolean[] hasUpdates;

    public int currentYIndexForBatch;
    public boolean useOreScan;
    public boolean isActive;
    public int lifetime;

    public Map<EntityType<?>, EntityInfo> localEntities;
    public boolean hasPlayer;

    private ChunkDisplayInfo() {
        this.currentYIndexForBatch = -1;
        this.isActive = true;
        this.useOreScan = false;
        this.hasPlayer = false;
        this.lifetime = 0;

        hasUpdates = new boolean[16];
        localEntities = new HashMap<>();
    }


    public ChunkDisplayInfo(LevelChunk chunk, TripleInt blockOffset, boolean useOreScan)
    {
        this();
        this.levelSectionIndex = blockOffset.y;
        this.chunk = chunk;
        this.blockOffset = blockOffset;
        this.useOreScan = useOreScan;

        holoBits = new int[4096];
        if(useOreScan) {
            oreScanBits = new int[4096];
            updateBits(oreScanBits, this, true);
            return;
        }
        updateBits(holoBits, this, false);
    }

    //** GETTERS / SETTERS **//
    void setUseOreScan(boolean useOreScan) {
        this.useOreScan = useOreScan;
    }

    public int[] getHoloBits() {
        return holoBits;
    }

    public int[] getOreScanBits() {
        return oreScanBits;
    }

    public int augmentBatch() {
        currentYIndexForBatch++;
        if(currentYIndexForBatch > 15) currentYIndexForBatch = 0;
        return currentYIndexForBatch;
    }

    public void resetUpdates() {
        for(int i=0; i<16; i++) hasUpdates[i] = true;
        lifetime = 0;
        this.isActive = true;
    }

    public void refreshBits(boolean force) {
        lifetime = 0;
        this.isActive = true;
        if(chunk == null) return;
        if( force  || chunk.isUnsaved())
        {
            if(useOreScan) {
                updateBits(holoBits, this, false);
            } else {
                if(oreScanBits == null) oreScanBits = new int[4096];
                updateBits(oreScanBits, this, true);
            }
        }


        if(force)
            this.resetUpdates();
    }

    public void tick() {
        if (!isActive)
            lifetime++;
    }


    //** STATICS

    static Set<Integer> updateBits(int[] holoBits, ChunkDisplayInfo info, boolean useOreScan)
    {
        LevelChunk chunk = info.chunk;
        LevelChunkSection section = chunk.getSections()[info.levelSectionIndex];
        //Set<Integer> changed = new HashSet<>(64);
        for (int y = 0; y < 16; y++) {
            //info.hasUpdates[y] = false;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockState originalBlock = section.getBlockState(x, y, z);
                    int p = ISatelliteDisplayBE.getCachePos(x, y, z);
                    int temp;

                    if(useOreScan && ChiselBitsAPI.HOLO_ORE_BLOCK_INDEX(originalBlock.getBlock()) > -1) {
                        temp = ChiselBitsAPI.HOLO_ORE_BLOCK_INDEX(originalBlock.getBlock());
                    } else if (IGNORE.contains(originalBlock.getBlock())) {
                        temp = 0;
                    } else if( DARK.contains(originalBlock.getBlock()) ) {
                        temp = 4;
                    } else {
                        temp = 2;
                    }

                    info.hasUpdates[y] |= (holoBits[p]!=temp);
                    holoBits[p] = temp;
                }
            }
        }
        return null;
    }



    public void delta(int dx, int dy, int dz) {
        blockOffset.x += dx;
        blockOffset.y += dy;
        blockOffset.z += dz;
    }

    /**
     * returns true if the entity is new to this chunkInfo and should be accepted, false otherwise
     * @param e
     * @return
     */
    public boolean acceptLocalEntity(Entity e)
    {

        if( !ModConfig.getHerdEntities().contains(e.getType()) ) {
            return true;
        }

        EntityInfo info = localEntities.get(e.getType());
        if(info == null) {
            info = new EntityInfo();
            info.target = e;
            info.others.add(e);
            localEntities.put(e.getType(), info);
            return false;
        }

        info.others.add(e);
        if(info.target == null) {
            info.target = e;
            return false;
        }

        return info.others.size() == SatelliteMain.CONFIG.
            entityPings.minHerdCountThreshold;
    }

    public void clearEntities() {
        for(EntityInfo info : localEntities.values()) {
            info.others.clear();
            Entity e = info.target;
            if( !(e instanceof LivingEntity) || !e.isAlive() || e.isRemoved() )
                info.target = null;
        }

    }


    static class EntityInfo {
        public Entity target;
        public HashSet<Entity> others = new HashSet<>();
    }




}
