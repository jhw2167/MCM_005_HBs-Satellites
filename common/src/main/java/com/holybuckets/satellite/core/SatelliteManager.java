package com.holybuckets.satellite.core;

import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import io.netty.util.collection.IntObjectHashMap;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class SatelliteManager {

    /** Maps colorId to satellite block entity */
    public static final IntObjectHashMap<SatelliteBlockEntity> SATELLITES = new IntObjectHashMap<>(24);

    private static final Map<ChunkPos, LevelChunk> CHUNK_CACHE = new HashMap<>();

    public static void init(EventRegistrar reg) {
        reg.registerOnServerStopped(SatelliteManager::onServerStopped);

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
    }


    public static void remove(int colorId) {
        SATELLITES.remove(colorId);
    }
}
