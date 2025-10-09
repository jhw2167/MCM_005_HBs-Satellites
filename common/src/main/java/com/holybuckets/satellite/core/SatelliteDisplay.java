package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.config.SatelliteConfig;
import com.holybuckets.satellite.particle.ModParticles;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
    int zOffset;
    int xOffset;
    ChunkPos target;
    int currentSection;
    int depth;
    boolean needsUpdate;
    Map<BlockPos, ISatelliteDisplayBlock> displayBlocks;
    Set<Entity> displayEntities;
    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minZ = Integer.MAX_VALUE;
    private int maxZ = Integer.MIN_VALUE;

    private static SatelliteConfig CONFIG;

    public SatelliteDisplay(Level level, SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller)
    {

        this.level = level;
        this.satellite = satellite;
        this.controller = controller;

        this.zOffset = 0;
        this.xOffset = 0;
        this.depth = 2;
        this.needsUpdate = true;
        displayBlocks = new HashMap<>();
        displayEntities = new HashSet<>();
        if(satellite == null) return;

        this.target = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );
        resetChunkSection();
        CONFIG = SatelliteMain.CONFIG;
    }


    //** GETTERS SETTERS

    public int getDepth() {
        return this.depth;
    }

    public int getCurrentSection() {
        return currentSection;
    }


    public void adjOrdinal(int dNS, int dEW) {
        zOffset -= dNS;
        xOffset -= dEW;
        ChunkPos satellitePos = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );
        this.target = new ChunkPos(satellitePos.x + xOffset, satellitePos.z + zOffset);
        this.needsUpdate = true;
    }

    public void adjDepth(int delta) {
         int temp = this.depth + delta;
         if(temp < 1 || temp > 4) return;
        this.depth += delta;
        this.needsUpdate = true;
    }

    public void setDepth(int newDepth) {
        if(newDepth < 1)
            newDepth = 1;
        if(newDepth > 4)
            newDepth = 4;
        this.depth = newDepth;
        this.needsUpdate = true;
    }

    public void setCurrentSection(int section) {
        this.currentSection = section;
        this.needsUpdate = true;
    }

    public void adjCurrentSection(int delta) {
        int temp = this.currentSection + delta;
        if( temp < 0 ||  temp >= level.getSectionsCount()-depth ) return;
        this.currentSection = temp;
        this.needsUpdate = true;
    }

    public void resetChunkSection() {
        if(noSource() || this.target == null) return;
        LevelChunkSection[] sections =  level.getChunk(target.x, target.z).getSections();
        for (int i = sections.length - 1; i >= 1; i--) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            this.currentSection = i;
            break;
        }
        this.needsUpdate = true;
    }

    public void resetOrdinal() {
        this.zOffset = 0;
        this.xOffset = 0;
        if(noSource() || this.satellite == null) return;
        this.target = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );
        this.needsUpdate = true;
    }

    public boolean needsClear() { return needsUpdate; }

    public void resetDisplayUpdates() {
        if(needsUpdate) {
            INFO_CACHE.values().forEach( info -> {
                if(info.isActive) info.resetUpdates();
            });
        }
        this.needsUpdate = false;
    }

    //** DISPLAY METHODS

    public static boolean hasActiveDisplay(LevelChunk chunk) {
        for(ChunkDisplayInfo info : INFO_CACHE.values()) {
            if(info.chunk == chunk && info.isActive) return true;
        }
        return false;
    }

    public boolean noSource() { return satellite == null; }

    public void add(BlockPos blockPos, ISatelliteDisplayBlock displayBlock) {
        displayBlocks.put(blockPos, displayBlock);
        updateBounds(blockPos);
    }

    private void updateBounds(BlockPos pos) {
        minX = Math.min(minX, pos.getX());
        maxX = Math.max(maxX, pos.getX());
        minZ = Math.min(minZ, pos.getZ());
        maxZ = Math.max(maxZ, pos.getZ());
    }

    private void recalculateBounds() {
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minZ = Integer.MAX_VALUE;
        maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : displayBlocks.keySet()) {
            updateBounds(pos);
        }
    }

    //** Do not truncate the Vec3, bounds should be forgiving
    public boolean isHitWithinDisplay(Vec3 hit) {
        // Small epsilon for floating point comparison tolerance
        double epsilon = 0.001;

        // Adjust boundaries to account for block edges
        // minX is the block position, so valid hits range from minX to minX+1
        double adjustedMinX = minX - epsilon;
        double adjustedMaxX = maxX + 1.0 + epsilon;
        double adjustedMinZ = minZ - epsilon;
        double adjustedMaxZ = maxZ + 1.0 + epsilon;

        double adjustedMinY = controller.getBlockPos().getY() + 1.0 - epsilon;
        double adjustedMaxY = controller.getBlockPos().getY() + depth + 1.0 + epsilon;

        if (hit.x < adjustedMinX || hit.x > adjustedMaxX
            || hit.z < adjustedMinZ || hit.z > adjustedMaxZ
            || hit.y < adjustedMinY || hit.y > adjustedMaxY) {
            return false;
        }
        return true;
    }

    public void addAll(Map<BlockPos, ISatelliteDisplayBlock> blocks) {
        displayBlocks.putAll(blocks);
        for (BlockPos pos : blocks.keySet()) {
            updateBounds(pos);
        }
    }

    public void remove(BlockPos blockPos) {
        displayBlocks.remove(blockPos);
        recalculateBounds();
    }

    public boolean contains(BlockPos blockPos) {
        return displayBlocks.containsKey(blockPos);
    }

    public void clear() {
        displayBlocks.clear();
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minZ = Integer.MAX_VALUE;
        maxZ = Integer.MIN_VALUE;
    }

    public BlockPos getOffset(BlockPos blockPos) {
        return blockPos.subtract( controller.getBlockPos() );
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

    public ChunkDisplayInfo getDisplayInfo(SatelliteDisplayBlockEntity displayblock, int section)
    {
        BlockPos dispOffset = getOffset(displayblock.getBlockPos());

        TripleInt chunkSelection = new TripleInt(target.x + dispOffset.getX(), section, target.z + dispOffset.getZ());
        if(INFO_CACHE.containsKey(chunkSelection)) {
            ChunkDisplayInfo info = INFO_CACHE.get(chunkSelection);
            if(!info.isActive) info.refreshBits(true);
            info.isActive = true;
            return info;
        }

        LevelChunk chunk = SatelliteManager.getChunk((ServerLevel) level, chunkSelection.x, chunkSelection.z);
        if(chunk == null) return null;
        if(chunk.getSections().length <= section) return null;

        INFO_CACHE.put(chunkSelection, new ChunkDisplayInfo(chunk, section));
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
        displayEntities.clear();

        if(this.needsUpdate)
        {
            BlockPos cntrlPos = controller.getBlockPos();
            int xChunkStart = (maxX - cntrlPos.getX()) + target.x;
            int xChunkEnd = (minX - cntrlPos.getX()) + target.x;

            int zChunkStart = (maxZ - cntrlPos.getZ()) + target.z;
            int zChunkEnd = (minZ - cntrlPos.getZ()) + target.z;

            int xStart = level.getChunk(xChunkStart, zChunkStart).getPos().getMinBlockX();
            int zStart = level.getChunk(xChunkStart, zChunkStart).getPos().getMinBlockZ();

            int xEnd = level.getChunk(xChunkEnd, zChunkEnd).getPos().getMaxBlockX();
            int zEnd = level.getChunk(xChunkEnd, zChunkEnd).getPos().getMaxBlockZ();

            int yEnd = (16*currentSection+1) + level.getMinBuildHeight();
            int yStart = yEnd - (16*depth);

            AABB aabb = new AABB(
                xStart, yStart, zStart,
                xEnd, yEnd, zEnd
            );

            // Query entities in this AABB (living entities only)
            List<Entity> entitiesInArea = level.getEntities(
                (Entity) null, aabb, this::entityPredicate
            );

            for(Entity e : entitiesInArea) {
                addEntity(e);
            }

        }

        displayEntities.addAll(HBUtil.PlayerUtil.getAllPlayers());

    }

        private static HashSet<EntityType<?>> BLACKLISTED_ENTITIES = new HashSet<>();
        private boolean entityPredicate(Entity e) {
            if( !(e instanceof LivingEntity) || !e.isAlive() || e.isRemoved() || displayEntities.contains(e) ) return false;
            if( e instanceof ServerPlayer ) return false;

            if(CONFIG.hostileEntityTypes.contains(e.getType())) {}
            else if(CONFIG.friendlyEntityTypes .contains(e.getType())) {}
            else if(CONFIG.neutralEntityTypes.contains(e.getType()  )) {}
            else if(CONFIG.herdEntityTypes.contains(e.getType()     )) {}
            else return false;

            //ChunkDisplayInfo info = INFO_CACHE.get(new TripleInt(e.chunkPosition().x, currentSection, e.chunkPosition().z ));
            //return info.acceptLocalEntity(e);
            return true;
        }



    final static float RENDER_SCALE = 0.0625f; // 1/16
    public void renderEntities(BlockPos cntrlPos)
    {
        if(cntrlPos == null || noSource() || this.target == null) return;

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

            //Check if this block is in displayBlocks using y coord from cntrlPos
            if(!displayBlocks.containsKey(new BlockPos(
                cntrlPos.getX() + chunkOffsetX,
                cntrlPos.getY(),
                cntrlPos.getZ() + chunkOffsetZ
            ))) continue;


            final int Y_MIN = level.getMinBuildHeight();
            int blockOffsetY = e.blockPosition().getY() - (((currentSection) * 16)+Y_MIN) + ((depth-1)*16);
            if(blockOffsetY < 0 ) { //under the table
                continue;
            } else if( blockOffsetY > (16*depth) ) { //above the table
                if(!(e instanceof ServerPlayer)) continue; //only show players above the table
                blockOffsetY = 16*(depth+1);
            }

            ParticleOptions particleType = getParticleType(e);
            float x = cntrlPos.getX() + chunkOffsetX + (blockOffsetX * RENDER_SCALE);
            float z = cntrlPos.getZ() + chunkOffsetZ + (blockOffsetZ * RENDER_SCALE);
            float y = cntrlPos.getY()+1.125f + (blockOffsetY * RENDER_SCALE);

            // check against Max and min values
            if(x < minX || x > maxX || z < minZ || z > maxZ) continue;

            ((ServerLevel) level).sendParticles(
                particleType,                     // Particle type
                x, y, z,
                1,                                // Particle count
                0.0, 0.0, 0.0,                   // X/Y/Z velocity/spread
                0.0                               // Speed
            );


        }
    }

    public void renderUI(ServerPlayer p, BlockHitResult hitResult)
    {
        //Render flame particle effect at cursor
        Vec3 cursorPos = hitResult.getLocation();
        BlockPos blockPos = hitResult.getBlockPos();

        int color = ChiselBitsAPI.DEMARCATOR(p);
        ((ServerLevel) level).sendParticles(
            ModParticles.hoverOrange,                     // Particle type
            cursorPos.x, cursorPos.y-RENDER_SCALE, cursorPos.z,
            1,                                // Particle count
            0.0, 0.0, 0.0,                   // X/Y/Z velocity/spread
            0.0                               // Speed
        );

        BlockPos cntrlPos = controller.getBlockPos();
        BlockPos blockOffset = blockPos.subtract(cntrlPos);
        BlockPos displayInfoPos = cntrlPos.offset(
            blockOffset.getX(), 0, blockOffset.getZ() );
        TripleInt infoKey = new TripleInt(
        blockOffset.getX()+target.x,
        blockOffset.getY()+(this.currentSection-depth),
        blockOffset.getZ()+target.z
        );

        ChunkDisplayInfo info = INFO_CACHE.get(infoKey);
        Vec3 internalTarget = ChiselBitsAPI.clamp(cursorPos, displayInfoPos);

        //internalTarget will always be relative to x,y,z: 0 -> 1 axis
        // but we need to convert to distant block offsets in the appropriate axis
        TripleInt chunkBlockOffset = new TripleInt(
            (int) internalTarget.x * 16,      //0.7235 into the block means 16*0.7235 = 11 blocks displayed from the chunkPos
            (int) internalTarget.y * 16,
            (int) internalTarget.z * 16
        );
        if( info == null) return;

        BlockPos startPos = new HBUtil.WorldPos(chunkBlockOffset,
         info.levelSectionIndex, info.chunk).getWorldPos();

        //ChiselBitsAPI.highlightLocalArea(level, cursorPos, displayInfoPos, color );

    }


    //** STATICS

    public static void init(EventRegistrar reg) {
        reg.registerOnServerTick(TickType.ON_20_TICKS , SatelliteDisplay::onServerTick);
        reg.registerOnServerStopped((event) -> INFO_CACHE.clear());
        reg.registerOnBeforeServerStarted(SatelliteDisplay::loadParticleTypes);
    }

    private static ParticleOptions getParticleType(Entity e) {
        return PARTICLE_TYPE_MAP.getOrDefault(e.getType(), ParticleTypes.MYCELIUM );
    }

    private static final int MAX_LIFETIME = 300; // 300s
    private static void onServerTick(ServerTickEvent event) {

        Iterator<Map.Entry<TripleInt, ChunkDisplayInfo>> iterator = INFO_CACHE.entrySet().iterator();
        LongOpenHashSet chunksContainPlayer = new LongOpenHashSet();
        HBUtil.PlayerUtil.getAllPlayers().forEach( p -> {
            chunksContainPlayer.add( HBUtil.ChunkUtil.getChunkPos1DMap(p.chunkPosition()) );
        });

        while (iterator.hasNext())
        {
            Map.Entry<TripleInt, ChunkDisplayInfo> entry = iterator.next();
            ChunkDisplayInfo info = entry.getValue();

            long chunkId = HBUtil.ChunkUtil.getChunkPos1DMap(info.chunk.getPos());
            info.hasPlayer = chunksContainPlayer.contains(chunkId);
            if(info.isActive) continue;
            info.tick();
            if(info.lifetime > MAX_LIFETIME) {
                iterator.remove();
                SatelliteManager.flagChunkForUnload(info.chunk.getPos());
                continue;
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

        PARTICLE_TYPE_MAP.put(EntityType.PLAYER, ModParticles.basePing);
        PARTICLE_TYPE_MAP.put(EntityType.SLIME, ParticleTypes.ITEM_SLIME);
    }


}
