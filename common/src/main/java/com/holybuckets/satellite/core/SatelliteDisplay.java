package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import org.antlr.v4.runtime.misc.MultiMap;

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
    Map<BlockPos, ISatelliteDisplayBlock> displayBlocks;
    Set<Entity> displayEntities;

    public SatelliteDisplay(Level level, SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller) {
        this.level = level;
        this.satellite = satellite;
        this.controller = controller;
        displayBlocks = new HashMap<>();
        displayEntities = new HashSet<>();
        if(satellite == null) return;

        this.target = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );

        LevelChunkSection[] sections =  level.getChunk(target.x, target.z).getSections();
        for (int i = sections.length - 1; i >= 1; i--) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            this.currentSection = i;
            break;
        }
        this.depth = 1;
    }

    public static boolean hasActiveDisplay(LevelChunk chunk) {
        for(ChunkDisplayInfo info : INFO_CACHE.values()) {
            if(info.chunk == chunk && info.isActive) return true;
        }
        return false;
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
        LevelChunk chunk = SatelliteManager.getChunk((ServerLevel) level, chunkSelection.x, chunkSelection.z);
        if(chunk == null) return null;
        if(chunk.getSections().length <= section) return null;

        ChunkDisplayInfo info = INFO_CACHE.putIfAbsent(chunkSelection, new ChunkDisplayInfo(chunk, section));
        if(info != null) {
            info.resetUpdates();
            if(!info.isActive)
                info.refreshBits();
            info.isActive = true;
        }
        return INFO_CACHE.get(chunkSelection);

    }


    public void addEntity(Entity e) {
        if(e == null) return;
        displayEntities.add(e);
    }

    /**
     * Collects all entities in all observed chunks in this area
     */
    public void collectEntities()
    {

        if(noSource() || this.target == null) return;

        for( ChunkDisplayInfo info : INFO_CACHE.values() )
        {
            if(!info.isActive || info.chunk != null) continue;
            int xStart = info.chunk.getPos().getMinBlockX();
            int zStart = info.chunk.getPos().getMinBlockZ();

            int xEnd = info.chunk.getPos().getMaxBlockX();
            int zEnd = info.chunk.getPos().getMaxBlockZ();

            int yStart = 16*info.levelSectionIndex + level.getMinBuildHeight();
            int yEnd = yStart + 15;

            AABB aabb = new AABB(
                xStart, yStart, zStart,
                xEnd, yEnd, zEnd
            );

            // Query entities in this AABB (living entities only)
            List<Entity> entitiesInArea = level.getEntities(
                (Entity) null, aabb,
                entity -> {return (entity instanceof LivingEntity);}
            );

            for(Entity e : entitiesInArea) {
                addEntity(e);
            }
        }

    }


    final static float RENDER_SCALE = 0.0625f; // 1/16
    public void renderEntities(BlockPos start)
    {
        if(start == null || noSource() || this.target == null) return;

        //Obtain an iterator to the entity list
        Iterator<Entity> iterator = displayEntities.iterator();
        while (iterator.hasNext())
        {
            Entity e = iterator.next();
            if(e == null || !e.isAlive() || e.isRemoved()) {
                iterator.remove(); continue;
            }

            int chunkOffsetX = e.chunkPosition().x - target.x;
            int chunkOffsetZ = e.chunkPosition().z - target.z;

            BlockPos chunkWorldPos = e.chunkPosition().getWorldPosition();
            int blockOffsetX = e.blockPosition().getX() - chunkWorldPos.getX();
            int blockOffsetZ = e.blockPosition().getZ() - chunkWorldPos.getZ();

            final int Y_MIN = level.getMinBuildHeight();
            int blockOffsetY = e.blockPosition().getY() - (((currentSection) * 16)+Y_MIN) + ((depth-1)+16);
            if(blockOffsetY < 0 ) { //under the table
                continue;
            }

            ParticleOptions particleType = getParticleType(e);
            float x = start.getX() + chunkOffsetX + (blockOffsetX * RENDER_SCALE);
            float z = start.getZ() + chunkOffsetZ + (blockOffsetZ * RENDER_SCALE);
            float y = start.getY()+1 + (blockOffsetY * RENDER_SCALE);
            ((ServerLevel) level).sendParticles(
                particleType,                     // Particle type
                x, y, z,
                1,                                // Particle count
                0.0, 0.0, 0.0,                   // X/Y/Z velocity/spread
                0.0                               // Speed
            );

        }
    }



    //** STATICS

    public static void init(EventRegistrar reg) {
        reg.registerOnServerTick(TickType.ON_20_TICKS , SatelliteDisplay::onServerTick);
        reg.registerOnServerStopped((event) -> INFO_CACHE.clear());
        reg.registerOnBeforeServerStarted(SatelliteDisplay::loadParticleTypes);
    }

    private static ParticleOptions getParticleType(Entity e) {
        return PARTICLE_TYPE_MAP.getOrDefault(e.getType(), ParticleTypes.ELECTRIC_SPARK);
    }

    private static final int MAX_LIFETIME = 300; // 300s
    private static void onServerTick(ServerTickEvent event) {

        Iterator<Map.Entry<TripleInt, ChunkDisplayInfo>> iterator = INFO_CACHE.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<TripleInt, ChunkDisplayInfo> entry = iterator.next();
            ChunkDisplayInfo info = entry.getValue();
            if(info.isActive) continue;
            info.tick();
            if(info.lifetime > MAX_LIFETIME) {
                iterator.remove();
                SatelliteManager.flagChunkForUnload(info.chunk.getPos());
            }
        }
    }

    private static Map<EntityType, ParticleOptions > PARTICLE_TYPE_MAP = new HashMap<>();
    private static void loadParticleTypes(ServerStartingEvent  event) {

        //For all entities in registry, test if it is hostile mob, the load as ParticleTypes.FLAME
        for(EntityType<?> type : event.getServer().registryAccess().registryOrThrow(Registries.ENTITY_TYPE)) {
            if(!type.getCategory().isFriendly()) {
                PARTICLE_TYPE_MAP.put(type, ParticleTypes.FLAME);
            }
        }

    }

}
