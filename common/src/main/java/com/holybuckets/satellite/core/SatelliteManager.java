package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import io.netty.util.collection.IntObjectHashMap;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class SatelliteManager {

    /** Maps colorId to satellite block entity */
    public static final IntObjectHashMap<SatelliteBlockEntity> SATELLITES = new IntObjectHashMap<>(24);

    private static final Map<ChunkPos, CachedChunkInfo> CHUNK_CACHE = new HashMap<>();
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
        if(SATELLITES.containsKey(colorId)) return;
        SATELLITES.put(colorId, be);
    }

    //** Events
    private static void onServerStopped(ServerStoppedEvent event) {
        SATELLITES.clear();
        // Unforce load all chunks before clearing cache
        CHUNK_CACHE.forEach((pos, info) -> {
            if (info.forceLoaded) {
                HBUtil.ChunkUtil.unforceLoadChunk((ServerLevel)info.chunk.getLevel(), pos);
            }
        });
        CHUNK_CACHE.clear();
    }

    private static void onServerTick(ServerTickEvent event) {
        Iterator<Map.Entry<ChunkPos, CachedChunkInfo>> iterator = CHUNK_CACHE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, CachedChunkInfo> entry = iterator.next();
            CachedChunkInfo info = entry.getValue();
            info.lifetime++;
            
            if (info.lifetime > MAX_CHUNK_LIFETIME) {
                if (info.forceLoaded) {
                    HBUtil.ChunkUtil.unforceLoadChunk((ServerLevel)info.chunk.getLevel(), entry.getKey());
                }
                iterator.remove();
            }
        }
    }

    public static LevelChunk getChunk(ServerLevel level, ChunkPos pos) {
        CachedChunkInfo cachedInfo = CHUNK_CACHE.get(pos);
        if (cachedInfo != null) {
            cachedInfo.lifetime = 0;
            return cachedInfo.chunk;
        }

        // Try to get active chunk first
        LevelChunk chunk = level.getChunk(pos.x, pos.z, false);
        if (chunk != null) {
            CHUNK_CACHE.put(pos, new CachedChunkInfo(chunk, false));
            return chunk;
        }

        // Try force loading
        chunk = HBUtil.ChunkUtil.forceLoadChunk(level, pos);
        if (chunk != null) {
            CHUNK_CACHE.put(pos, new CachedChunkInfo(chunk, true));
            return chunk;
        }

        return null;
    }

    public static void flagChunkForUnload(ChunkPos pos) {
        CachedChunkInfo info = CHUNK_CACHE.get(pos);
        if (info != null) {
            info.lifetime = MAX_CHUNK_LIFETIME; // This will trigger unload on next tick
        }
    }


    public static void remove(int colorId) {
        SATELLITES.remove(colorId);
    }
}
