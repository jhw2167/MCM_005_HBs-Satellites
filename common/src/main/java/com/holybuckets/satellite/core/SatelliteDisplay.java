package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.satellite.CommonClass;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ITargetController;
import com.holybuckets.satellite.config.ModConfig;
import com.holybuckets.satellite.config.SatelliteConfig;
import com.holybuckets.satellite.item.SatelliteItemUpgrade;
import com.holybuckets.satellite.particle.ModParticles;
import com.holybuckets.satellite.particle.WoolDustHelper;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.UseBlockEvent;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    protected Level level;
    protected SatelliteControllerBlockEntity controller;
    protected ITargetController targetController;
    private SatelliteBlockEntity satellite;
    int zOffset;
    int xOffset;
    ChunkPos target;

    int currentSection;
    int maxSection;
    int depth;

    protected Map<BlockPos, ISatelliteDisplayBE> displayBlocks;
    protected int minX = Integer.MAX_VALUE;
    protected int maxX = Integer.MIN_VALUE;
    protected int minZ = Integer.MAX_VALUE;
    protected int maxZ = Integer.MIN_VALUE;

    protected SatelliteItemUpgrade[] upgrades;
    protected Set<ISatelliteControllerBE> controllerBlocks;

    //rendering
    Set<Entity> displayEntities;
    private Set<ISatelliteDisplayBE> rateLimiter;
    private static final int MAX_RENDER_PER_TICK = 5;

    boolean needsUpdate;
    boolean needsEntityUpdate;

    protected static SatelliteConfig CONFIG;
    public int lifetime;
    private BlockHitResult cursorPos;
    private BlockHitResult cursorSelection;

    //For Child client
    protected SatelliteDisplay(Level level,SatelliteControllerBlockEntity controller) {
        this(level, null, controller);
    }

    public SatelliteDisplay(Level level, SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller)
    {

        this.level = level;
        this.satellite = satellite;
        this.controller = controller;
        this.targetController = controller;

        this.zOffset = 0;
        this.xOffset = 0;
        this.depth = 2;
        this.needsUpdate = true;

        rateLimiter = new HashSet<>();
        displayEntities = new HashSet<>();

        displayBlocks = new HashMap<>();
        controllerBlocks = new HashSet<>();
        upgrades = new SatelliteItemUpgrade[4];
        if(satellite == null) return;

        this.target = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );
        resetChunkSection();
        CONFIG = SatelliteMain.CONFIG;
    }


    //** GETTERS SETTERS

    public SatelliteControllerBlockEntity getSatelliteController() {
        return controller;
    }

    /**
     *  Add an upgrade to the satellite display, 4 max slots
     *  returns the previous upgrade in the slot, or the upgrade added if slot was empty
     */
    public SatelliteItemUpgrade addUpgrade(SatelliteItemUpgrade upgrade, int slot) {
        if(slot > upgrades.length ) return null;
        SatelliteItemUpgrade temp = upgrades[slot];
        upgrades[slot] = upgrade;
        if(temp != null) return temp;
        return upgrade;
    }

    public SatelliteItemUpgrade removeUpgrade(int slot) {
        if(slot > upgrades.length ) return null;
        return upgrades[slot];
    }

    public SatelliteItemUpgrade[] getUpgrades() {
        return upgrades;
    }


    public int getDepth() {
        return this.depth;
    }

    public int getCurrentSection() {
        return currentSection;
    }


    public void adjOrdinal(int dNS, int dEW)
    {
        if(noSource() || this.satellite == null) return;
        if(dNS == 0 && dEW == 0) return;
        zOffset += dNS;
        xOffset += dEW;
        ChunkPos satellitePos = HBUtil.ChunkUtil.getChunkPos( satellite.getBlockPos() );
        this.target = new ChunkPos(satellitePos.x + xOffset, satellitePos.z + zOffset);
        this.needsUpdate = true;
    }

    private static int MAX_DEPTH = 4;
    public void adjDisplayDepth(int dDepth) {
        if(noSource() || this.satellite == null) return;
        if(dDepth == 0) return;
        this.depth += dDepth;
        setDepth( this.depth );
        this.needsUpdate = true;
    }

    public void setDepth(int newDepth) {
        if(noSource() || this.satellite == null) return;
        if(newDepth == this.depth) return;
        if(newDepth <= 1)             { depth = 1; }
        else if(newDepth > MAX_DEPTH) { depth = MAX_DEPTH; }
        else                          { this.depth = newDepth; }
        this.needsUpdate = true;
    }

    public void setCurrentSection(int section) {
        if( section < depth ||  section > maxSection ) return;
        if( section == this.currentSection ) return;
        this.currentSection = section;
        this.needsUpdate = true;
    }

    public void adjCurrentSection(int delta) {
        if(noSource() || this.satellite == null) return;
        if( delta == 0 ) return;
        int section = this.currentSection + delta;
        setCurrentSection( section );
    }


    private void setPosition(Player p, BlockHitResult res)
    {
        if(this.targetController == null) return;
        //Convert cursorPos to overworld block position using offsets
        //calculate chunkOffset from satellite, then blockOffset from fractional cursorPos
        if(res == null || p == null) return;
        Vec3 playerEye = p.getEyePosition(1.0f);
        Vec3 cursorToEye = playerEye.subtract(res.getLocation()).normalize();
        this.cursorSelection = new BlockHitResult(
            res.getLocation().add(cursorToEye.scale(RENDER_SCALE)),
            res.getDirection(), res.getBlockPos(), res.isInside()
        );

        BlockPos holoBlock = cursorSelection.getBlockPos();
        Vec3 hitLoc = cursorSelection.getLocation();
        Vec3i controllerOffset = this.getOffset( holoBlock );
        //Calculate chunk offset from satellite position considering controller offset and any ordinal shifts
        Vec3i chunkSecOffset = new Vec3i(
            target.x + controllerOffset.getX(),
            ( currentSection - depth ) + controllerOffset.getY(),
            target.z + controllerOffset.getZ()
        );

        Vec3 blockOffset = new Vec3(
            (hitLoc.x - holoBlock.getX()) * 16,
            (hitLoc.y - holoBlock.getY()) * 16,
            (hitLoc.z - holoBlock.getZ()) * 16
        );

        int yOffset = HBUtil.WorldPos.sectionIndexToY(chunkSecOffset.getY(),
         this.level.getMinBuildHeight() ) + (int) blockOffset.y;
        BlockPos blockTarget = level.getChunk(chunkSecOffset.getX(), chunkSecOffset.getZ())
            .getPos().getBlockAt(
                (int) blockOffset.x,
                (int) yOffset,
                (int) blockOffset.z
            );

        this.targetController.setUiPosition(blockTarget);
        this.targetController.setCursorPosition(hitLoc);
    }


    public void setTargetController(ITargetController targetingController) {
        this.targetController =  targetingController;
    }

    public void fire(Player p, ITargetController targetingController) {

    }

    //** DISPLAY METHODS
    public void resetChunkSection()
    {
        if(noSource() || this.satellite == null || this.target == null) return;
        LevelChunkSection[] sections =  level.getChunk(target.x, target.z).getSections();

        maxSection = HBUtil.WorldPos.yToSectionIndex(
            satellite.getBlockPos().getY(), level.getMinBuildHeight()) - 1;
        for (int i = maxSection; i >= 1; i--) {
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
    public void setNeedsUpdate(boolean b) { this.needsUpdate = b; }

    public void resetDisplayUpdates() {
        INFO_CACHE.values().forEach( info -> {
            if(info.isActive) info.resetUpdates();
        });
        needsUpdate = true;
        if(targetController != null) targetController.setUiPosition(null);
        this.cursorSelection = null;
    }


    public static boolean hasActiveDisplay(LevelChunk chunk) {
        for(ChunkDisplayInfo info : INFO_CACHE.values()) {
            if(info.chunk == chunk && info.isActive) return true;
        }
        return false;
    }

    public boolean noSource() {
        if( satellite == null ) return true;
        return !controller.isDisplayOn();
    }

    public void add(BlockPos blockPos, ISatelliteDisplayBE displayBlock) {
        displayBlocks.put(blockPos, displayBlock);
        if(displayBlock instanceof ISatelliteControllerBE cb) controllerBlocks.add( cb );
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

    public void addAll(Map<BlockPos, ISatelliteDisplayBE> blocks) {
        displayBlocks.putAll(blocks);
        for (BlockPos pos : blocks.keySet()) {
            if(displayBlocks.get(pos) instanceof  ISatelliteControllerBE cb)
                controllerBlocks.add( cb );
            updateBounds(pos);
        }
    }

    public void remove(BlockPos blockPos) {
        ISatelliteDisplayBE be = displayBlocks.remove(blockPos);
        if(be instanceof ISatelliteControllerBE cb) controllerBlocks.remove( cb );
        recalculateBounds();
    }

    public boolean contains(BlockPos blockPos) {
        return displayBlocks.containsKey(blockPos);
    }

    public void clear()
    {
        controllerBlocks.clear();
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
        SatelliteManager manager = SatelliteManager.get(level);
        LevelChunk chunk = manager.getChunk((ServerLevel) level, chunkSelection.x, chunkSelection.z);
        if(chunk == null) return null;
        if(chunk.getSections().length <= section) return null;

        INFO_CACHE.put(chunkSelection, new ChunkDisplayInfo(chunk, section));
        return INFO_CACHE.get(chunkSelection);
    }


    //** Render Methods

    public boolean testRender(ISatelliteDisplayBE disp) {
        if(disp.hasPlayer()) return true;
        if( rateLimiter.size() > MAX_RENDER_PER_TICK || rateLimiter.contains(disp)) return false;
        rateLimiter.add(disp);
        return true;
    }
    public void resetRateLimiter() {
        rateLimiter.clear();
    }

    /**
     * Collects all entities in all observed chunks in this area
     */
    public void collectEntities()
    {

        if(noSource() || this.target == null) return;


        if(this.needsUpdate)
        {
            this.needsEntityUpdate = true;
            displayEntities.clear();
            //for(ISatelliteDisplayBlock displayBlock : displayBlocks.values())
            {
                BlockPos cntrlPos = controller.getBlockPos();
                //*
                    //For total area entity collection
                int xChunkStart = (maxX - cntrlPos.getX()) + target.x;
                int xChunkEnd = (minX - cntrlPos.getX()) + target.x;

                int zChunkStart = (maxZ - cntrlPos.getZ()) + target.z;
                int zChunkEnd = (minZ - cntrlPos.getZ()) + target.z;

                SatelliteManager manager = SatelliteManager.get(level);
                LevelChunk startChunk = manager.getChunk((ServerLevel) level, xChunkStart, zChunkStart);
                LevelChunk endChunk = manager.getChunk((ServerLevel) level, xChunkEnd, zChunkEnd);
                if(startChunk == null || endChunk == null) return;

                int xStart, xEnd, zStart, zEnd;


                if (xChunkStart <= xChunkEnd) {
                    xStart = startChunk.getPos().getMinBlockX();
                    xEnd = endChunk.getPos().getMaxBlockX();
                } else {
                    LevelChunk extendedChunk = manager.getChunk((ServerLevel) level, xChunkEnd - 1, zChunkStart);
                    if(extendedChunk == null) return;
                    xStart = extendedChunk.getPos().getMinBlockX();
                    xEnd = startChunk.getPos().getMaxBlockX();
                }

                if (zChunkStart <= zChunkEnd) {

                    zStart = startChunk.getPos().getMinBlockZ();
                    zEnd = endChunk.getPos().getMaxBlockZ();
                } else {

                    LevelChunk extendedChunk = manager.getChunk((ServerLevel) level, xChunkStart, zChunkEnd - 1);
                    if(extendedChunk == null) return;
                    zStart = extendedChunk.getPos().getMinBlockZ();
                    zEnd = startChunk.getPos().getMaxBlockZ();
                }

                // Ensure min/max order is correct
                int minX = Math.min(xStart, xEnd);
                int maxX = Math.max(xStart, xEnd);
                int minZ = Math.min(zStart, zEnd);
                int maxZ = Math.max(zStart, zEnd);

                 //*/
                 /*
                //For each display block, query its chunk area
                BlockPos dispOffset = getOffset(displayBlock.getBlockPos());
                int xChunk = target.x + dispOffset.getX();
                int zChunk = target.z + dispOffset.getZ();
                LevelChunk chunk = manager.getChunk((ServerLevel) level, xChunk, zChunk);

                  */

                int yTop = cntrlPos.getY();
                int sectionTop = (16*currentSection+1) + level.getMinBuildHeight();
                int yStart = sectionTop - (16*(depth+1));

                AABB aabb = new AABB(
                    minX, yStart, minZ,
                    maxX, yTop,   maxZ
                );

                // Query entities in this AABB (living entities only)
                List<Entity> entitiesInArea = level.getEntities(
                    (Entity) null, aabb, this::entityPredicate
                );

                for(Entity e : entitiesInArea) {
                    addEntity(e);
                }

            }

            this.needsEntityUpdate = false;
        }

        displayEntities.addAll(HBUtil.PlayerUtil.getAllPlayers());

    }

        private boolean entityPredicate(Entity e) {
            if( !(e instanceof LivingEntity) || !e.isAlive() || e.isRemoved() || displayEntities.contains(e) ) return false;
            if( e instanceof ServerPlayer ) return false;

            if(ModConfig.getHostileEntities().contains(e.getType())) {}
            else if(ModConfig.getFriendlyEntities().contains(e.getType())) {}
            else if(ModConfig.getNeutralEntities().contains(e.getType())) {}
            else if(ModConfig.getHerdEntities().contains(e.getType())) {}
            else return false;

            if( ModConfig.getHerdEntities().contains(e.getType()) ) {
                ChunkDisplayInfo info = INFO_CACHE.get(new TripleInt(e.chunkPosition().x, currentSection , e.chunkPosition().z ));
                if(info == null) return false;
                return info.acceptLocalEntity(e);
            }
            return true;
        }

        public void addEntity(Entity e) {
            if(e == null) return;
            displayEntities.add(e);
        }

        private static ParticleOptions getParticleType(Entity e) {
            return PARTICLE_TYPE_MAP.getOrDefault(e.getType(), ParticleTypes.MYCELIUM );
        }


    final static float RENDER_SCALE = 0.0625f; // 1/16
    public void renderEntities(BlockPos cntrlPos)
    {
        if(cntrlPos == null || noSource() || this.target == null) return;
        if(this.needsEntityUpdate) collectEntities();
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

            ((ServerLevel) level).sendParticles (
                particleType,                     // Particle type
                x, y, z,
                1,                                // Particle count
                0.0, 0.0, 0.0,                   // X/Y/Z velocity/spread
                0.0                               // Speed
            );


        }
    }

    //public void renderUI(ServerPlayer p, HitResult hitResult)
    public void renderUI()
    {
        /*
            boolean blockHit = (hitResult instanceof BlockHitResult);
            boolean viewingHolo = false;
            if(blockHit && isHitWithinDisplay(hitResult.getLocation())) {
                viewingHolo = SatelliteMain.chiselBitsApi
                    .isViewingHoloBlock(p.level(), (BlockHitResult) hitResult );
            }
            if( blockHit && viewingHolo ) {
                this.cursorPos = (BlockHitResult) hitResult;
            } else {
                this.cursorPos = null;
            }
            */

        for(ISatelliteControllerBE ctrlBlock : controllerBlocks)
        {
            if(ctrlBlock instanceof ITargetController tc && tc.getCursorPosition() != null)
            {
                Vec3 hitLoc = tc.getCursorPosition();
                ((ServerLevel) level).sendParticles(
                    WoolDustHelper.getDust(tc.getTargetColorId()),                     // Particle type
                    hitLoc.x, hitLoc.y, hitLoc.z,
                    2,                                // Particle count
                    0.0, 0.0, 0.0,                   // X/Y/Z velocity/spread
                    0.0                               // Speed
                );
            }

        }


    }


    //** STATICS

    public static void init(EventRegistrar reg) {

        //Player Events
        reg.registerOnUseBlock(SatelliteDisplay::onBlockUsed);

        //Server
        reg.registerOnBeforeServerStarted(SatelliteDisplay::onBeforeServerStarted, EventPriority.Lowest);
        reg.registerOnServerTick(TickType.ON_20_TICKS , SatelliteDisplay::onServerTick);
        reg.registerOnServerStopped((event) -> INFO_CACHE.clear());
    }


    //** EVENTS

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
                SatelliteManager.flagChunkForUnload(info.chunk.getLevel(), info.chunk.getPos());
                continue;
            }

        }
    }

    private static void onBeforeServerStarted(ServerStartingEvent event) {
        INFO_CACHE.clear();
        loadParticleTypes();
    }

    private static Map<EntityType, ParticleOptions > PARTICLE_TYPE_MAP = new HashMap<>();
    private static void loadParticleTypes()
    {
        //For all entities in registry, test if it is hostile mob, the load as ParticleTypes.FLAME
        for(EntityType et : ModConfig.getHostileEntities()) {
            PARTICLE_TYPE_MAP.put(et, ModParticles.redPing);
        }

        for(EntityType et : ModConfig.getFriendlyEntities()) {
            PARTICLE_TYPE_MAP.put(et, ModParticles.greenPing);
        }

        for(EntityType et : ModConfig.getNeutralEntities()) {
            PARTICLE_TYPE_MAP.put(et, ModParticles.basePing);
        }

        for(EntityType et : ModConfig.getHerdEntities()) {
            PARTICLE_TYPE_MAP.put(et, ModParticles.basePing);
        }

        PARTICLE_TYPE_MAP.put(EntityType.PLAYER, ModParticles.basePing);
        PARTICLE_TYPE_MAP.put(EntityType.SLIME, ParticleTypes.ITEM_SLIME);
    }
    private static void onBlockUsed(UseBlockEvent useBlockEvent)
    {

        Level level = useBlockEvent.getLevel();
        if(level == null || level.isClientSide) return;
        if( useBlockEvent.getPlayer() == null ) return;
        if( useBlockEvent.getHand() != InteractionHand.MAIN_HAND ) return;
        if( !useBlockEvent.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).isEmpty() ) return;
        if(!SatelliteMain.chiselBitsApi.isChiseledBlock(level, useBlockEvent.getHitResult().getBlockPos() )) return;

        BlockHitResult res = (BlockHitResult) useBlockEvent.getHitResult();
        double reach = SatelliteControllerBlockEntity.REACH_DIST_BLOCKS;
        //BlockHitResult res = CommonClass.getAnyHitResult(level, useBlockEvent.getPlayer(), reach );
        if( res == null ) return;
        BlockPos pos = res.getBlockPos();

        BlockPos displayBlockPos = pos.below();
        int dDepth = 0;
        while( dDepth++ < MAX_DEPTH ) {
            if(level.getBlockEntity(displayBlockPos) instanceof ISatelliteDisplayBE)
                break;
            displayBlockPos = displayBlockPos.below();
        }

        if( level.getBlockEntity(displayBlockPos) instanceof SatelliteDisplayBlockEntity displayBlockEntity ) {
            SatelliteDisplay source = displayBlockEntity.getSource();
            if(source != null) { source.setPosition( useBlockEvent.getPlayer(), res ); }
        }

    }


    public void sendinput(Player p, InteractionHand hand, int cmd) {
        this.sendinput(p, hand, cmd, null);
    }

    public void sendinput(Player p, InteractionHand hand, int cmd, ISatelliteControllerBE block) {
        if(controller != null) {
            controller.processInput(p, hand, cmd, block);
        }
    }

}
