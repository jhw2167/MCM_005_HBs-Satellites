package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.foundation.model.ManagedChunk;
import com.holybuckets.foundation.model.ManagedChunkUtility;
import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import io.netty.util.collection.IntObjectHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class SatelliteManager {

    /** Maps colorId to satellite block entity */
    private final IntObjectHashMap<SatelliteBlockEntity> satellites = new IntObjectHashMap<>(24);
    private final Map<SourceKey, SatelliteDisplay> displaySources = new HashMap<>();

    private final Long2ObjectMap<CachedChunkInfo> chunkCache = new Long2ObjectOpenHashMap<>(128);
    private static final int MAX_CHUNK_LIFETIME = 300; // 300 seconds
    private static final int MAX_DISPLAY_LIFETIME = 300; // 300 seconds

    private final List<Block> woolIds = new ArrayList<>(64);
    private final Level level;


    private static class SourceKey {
        SatelliteBlockEntity satellite;
        SatelliteControllerBlockEntity controller;

        SourceKey(SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller) {
            this.satellite = satellite;
            this.controller = controller;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SourceKey)) return false;
            SourceKey other = (SourceKey) obj;
            return Objects.equals(satellite, other.satellite) &&
                   Objects.equals(controller, other.controller);
        }
    }

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

    //State variables
    private boolean anyControllerOn;
    private final boolean isServerSide;
    
    public SatelliteManager(Level level) {
        this.level = level;
        this.isServerSide = !level.isClientSide();
        initWoolIds();
    }

    public static void init(EventRegistrar reg) {
        reg.registerOnBeforeServerStarted(SatelliteManager::onServerStarting);
        reg.registerOnServerStopped(SatelliteManager::onServerStopped);
        reg.registerOnServerTick(TickType.ON_20_TICKS, SatelliteManager::onServerTick);

        //SatelliteDisplay
        SatelliteDisplay.init(reg);
    }


    public SatelliteBlockEntity get(int colorId) {
        return satellites.get(colorId);
    }

    public void put(int colorId, SatelliteBlockEntity be) {
        if(be == null) return;
        satellites.putIfAbsent(colorId, be);
        be.setLevelChunk(getChunk(be.getLevel(), be.getBlockPos()));
    }

    public void remove(int colorId) {
        if(colorId < 0) return;
        satellites.remove(colorId);
    }

    public int totalSatellites() { return satellites.size(); }

    public int totalIds() {
        return woolIds.size();
    }

    public Block getWool(int id) {
        return new ArrayList<>(woolIds).get(id % woolIds.size());
    }

    public int getColorId(Block b) {
        return woolIds.indexOf(b);
    }

    public static ResourceLocation getResourceForColorId(int colorId) {
        Block wool = getWool(colorId);
        if(wool == null) return null;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(wool);
        ResourceLocation woolLoc = new ResourceLocation( blockId.getNamespace(),
            "block/" + blockId.getPath() );

        return woolLoc;
    }


    public static SatelliteDisplay generateSource(Level level, SatelliteBlockEntity satellite,
         SatelliteControllerBlockEntity controller)
    {
        anyControllerOn = true;
        SourceKey key = new SourceKey(satellite, controller);
        SatelliteDisplay satelliteDisplay = DISPLAY_SOURCES.get(key);
        if(satelliteDisplay != null) return satelliteDisplay;

        satelliteDisplay = new SatelliteDisplay(level, satellite, controller);
        satelliteDisplay.add(controller.getBlockPos(), controller);
        DISPLAY_SOURCES.put(key, satelliteDisplay);
        return satelliteDisplay;
    }


    //** Events
    private void initWoolIds() {
        woolIds.clear();
        woolIds.add(Blocks.RED_WOOL);
        woolIds.add(Blocks.ORANGE_WOOL);
        woolIds.add(Blocks.YELLOW_WOOL);
        woolIds.add(Blocks.LIME_WOOL);
        woolIds.add(Blocks.GREEN_WOOL);
        woolIds.add(Blocks.CYAN_WOOL);
        woolIds.add(Blocks.LIGHT_BLUE_WOOL);
        woolIds.add(Blocks.BLUE_WOOL);
        woolIds.add(Blocks.PURPLE_WOOL);
        woolIds.add(Blocks.MAGENTA_WOOL);
        woolIds.add(Blocks.PINK_WOOL);
        woolIds.add(Blocks.WHITE_WOOL);
        woolIds.add(Blocks.LIGHT_GRAY_WOOL);
        woolIds.add(Blocks.GRAY_WOOL);
        woolIds.add(Blocks.BROWN_WOOL);
        woolIds.add(Blocks.BLACK_WOOL);
    }

    public void shutdown() {
        satellites.clear();
        // Unforce load all chunks before clearing cache
        chunkCache.forEach((pos, info) -> {
            if (info.forceLoaded) {
                HBUtil.ChunkUtil.unforceLoadChunk((ServerLevel)info.chunk.getLevel(),
                    info.chunk.getPos(), Constants.MOD_ID);
            }
        });
        chunkCache.clear();
    }

    private static void onServerTick(ServerTickEvent event)
    {
        //Check lifetime of sources
        Iterator<Map.Entry<SourceKey, SatelliteDisplay>> sourceIterator = DISPLAY_SOURCES.entrySet().iterator();
        anyControllerOn = false;
        while (sourceIterator.hasNext())
        {
            Map.Entry<SourceKey, SatelliteDisplay> entry = sourceIterator.next();
            SatelliteDisplay display = entry.getValue();
            if(display == entry.getKey().controller.getSource()) {
                display.lifetime = 0; // Reset lifetime if any display is actively used
                anyControllerOn = true;
                continue;
            }
            display.lifetime++;

            if (display.lifetime > MAX_DISPLAY_LIFETIME) {
                display.clear();
                sourceIterator.remove();
            }
        }


        var iterator = CHUNK_CACHE.values().iterator();
        while (iterator.hasNext())
        {
            CachedChunkInfo info = iterator.next();
            if(SatelliteDisplay.hasActiveDisplay(info.chunk)) {
                info.lifetime = 0; // Reset lifetime if chunk is actively used
                continue;
            }
            info.lifetime++;
            
            if (info.lifetime > MAX_CHUNK_LIFETIME) {
                if (info.forceLoaded) {
                    HBUtil.ChunkUtil.unforceLoadChunk((ServerLevel)info.chunk.getLevel(),
                     info.chunk.getPos(), Constants.MOD_ID);
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
        long posKey = HBUtil.ChunkUtil.getChunkPos1DMap(pos);
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
        HBUtil.ChunkUtil.forceLoadChunk(level, pos, Constants.MOD_ID);
        if (chunk != null) {
            LevelChunk levelChunk = chunk.getLevelChunk();
            CHUNK_CACHE.put(posKey, new CachedChunkInfo(levelChunk, true));
            return levelChunk;
        }

        return null;
    }

    public static void flagChunkForUnload(ChunkPos pos) {
        long posKey = HBUtil.ChunkUtil.getChunkPos1DMap(pos);
        CachedChunkInfo info = CHUNK_CACHE.get(posKey);
        if (info != null) {
            info.lifetime = MAX_CHUNK_LIFETIME; // This will trigger unload on next tick
        }
    }


    public static boolean isAnyControllerOn() {
        return  anyControllerOn;
    }
}
