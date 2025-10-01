package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.foundation.model.ManagedChunk;
import com.holybuckets.foundation.model.ManagedChunkUtility;
import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import io.netty.util.collection.IntObjectHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class SatelliteManager {

    /** Maps colorId to satellite block entity */
    public static final IntObjectHashMap<SatelliteBlockEntity> SATELLITES = new IntObjectHashMap<>(24);

    private static final Long2ObjectMap<CachedChunkInfo> CHUNK_CACHE = new Long2ObjectOpenHashMap<>(128);
    private static final int MAX_CHUNK_LIFETIME = 300; // 300 seconds
    
    private static class CachedChunkInfo {
        LevelChunk chunk;
        int lifetime;
        boolean forceLoaded;
        
        CachedChunkInfo(LevelChunk chunk, boolean forceLoaded) {
            this.chunk = chunk;
            this.lifetime = 0;
            this.forceLoaded = forceLoaded;
        }
    }

    public static void init(EventRegistrar reg) {
        reg.registerOnServerStopped(SatelliteManager::onServerStopped);
        reg.registerOnServerTick(TickType.ON_20_TICKS, SatelliteManager::onServerTick);

        //SatelliteDisplay
        SatelliteDisplay.init(reg);
    }

    public static SatelliteBlockEntity get(int colorId) {
        return SATELLITES.get(colorId);
    }

    public static void put(int colorId, SatelliteBlockEntity be) {
        if(be == null) return;
        SATELLITES.putIfAbsent(colorId, be);
        be.setLevelChunk( getChunk(be.getLevel(), be.getBlockPos()) );
    }

    //** Events
    private static void onServerStopped(ServerStoppedEvent event) {
        SATELLITES.clear();
        // Unforce load all chunks before clearing cache
        CHUNK_CACHE.forEach((pos, info) -> {
            if (info.forceLoaded) {
                HBUtil.ChunkUtil.unforceLoadChunk((ServerLevel)info.chunk.getLevel(),
                    HBUtil.ChunkUtil.getId(info.chunk.getPos()), Constants.MOD_ID);
            }
        });
        CHUNK_CACHE.clear();
    }

    private static void onServerTick(ServerTickEvent event) {
        var iterator = CHUNK_CACHE.values().iterator();
        while (iterator.hasNext()) {
            CachedChunkInfo info = iterator.next();
            if(SatelliteDisplay.hasActiveDisplay(info.chunk)) {
                info.lifetime = 0; // Reset lifetime if chunk is actively used
                continue;
            }
            info.lifetime++;
            
            if (info.lifetime > MAX_CHUNK_LIFETIME) {
                if (info.forceLoaded) {
                    String chunkId = HBUtil.ChunkUtil.getId(info.chunk.getPos());
                    HBUtil.ChunkUtil.unforceLoadChunk((ServerLevel)info.chunk.getLevel(), chunkId, Constants.MOD_ID);
                }
                iterator.remove();
            }
        }
    }

    public static LevelChunk getChunk(Level level, BlockPos pos) {
        return getChunk((ServerLevel) level, HBUtil.ChunkUtil.getChunkPos(pos));
    }

    public static LevelChunk getChunk(ServerLevel level, int chunkX, int chunkZ) {
        return getChunk(level, new ChunkPos(chunkX, chunkZ));
    }

    public static LevelChunk getChunk(ServerLevel level, ChunkPos pos) {
        long posKey = HBUtil.ChunkUtil.getChunkPos1DMap(pos.x, pos.z);
        CachedChunkInfo cachedInfo = CHUNK_CACHE.get(posKey);
        if (cachedInfo != null) {
            cachedInfo.lifetime = 0;
            return cachedInfo.chunk;
        }

        // Try to get active chunk first
        String chunkId = HBUtil.ChunkUtil.getId(pos);
        ManagedChunk chunk = ManagedChunkUtility.getManagedChunk(level, chunkId);
        if (chunk != null) {
            CHUNK_CACHE.put(posKey, new CachedChunkInfo(chunk.getLevelChunk(), false));
            return chunk.getLevelChunk();
        }

        // Try force loading
        HBUtil.ChunkUtil.forceLoadChunk(level, chunkId, Constants.MOD_ID);
        if (chunk != null) {
            LevelChunk levelChunk = chunk.getLevelChunk();
            CHUNK_CACHE.put(posKey, new CachedChunkInfo(levelChunk, true));
            return levelChunk;
        }

        return null;
    }

    public static void flagChunkForUnload(ChunkPos pos) {
        long posKey = HBUtil.ChunkUtil.getChunkPos1DMap(pos.x, pos.z);
        CachedChunkInfo info = CHUNK_CACHE.get(posKey);
        if (info != null) {
            info.lifetime = MAX_CHUNK_LIFETIME; // This will trigger unload on next tick
        }
    }


    public static void remove(int colorId) {
        SATELLITES.remove(colorId);
    }
}
