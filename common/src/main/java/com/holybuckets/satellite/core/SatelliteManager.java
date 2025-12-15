package com.holybuckets.satellite.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.event.custom.ServerTickEvent;
import com.holybuckets.foundation.event.custom.SimpleMessageEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.foundation.model.ManagedChunk;
import com.holybuckets.foundation.model.ManagedChunkUtility;
import com.holybuckets.satellite.CommonClass;
import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.particle.WoolDustHelper;
import io.netty.util.collection.IntObjectHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.blay09.mods.balm.api.event.PlayerLoginEvent;
import net.blay09.mods.balm.api.event.TossItemEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class SatelliteManager {

    /** Maps colorId to satellite block entity */
    private final IntObjectHashMap<SatelliteBlockEntity> satellites;
    private final HashSet<SatelliteBlockEntity> satellitePositions;
    private final Map<SourceKey, SatelliteDisplay> displaySources;

    private final Long2ObjectMap<CachedChunkInfo> chunkCache = new Long2ObjectOpenHashMap<>(128);
    private static final int MAX_CHUNK_LIFETIME = 300; // 300 seconds
    private static final int MAX_DISPLAY_LIFETIME = 300; // 300 seconds
    private static boolean anyControllerOn;

    private static final Map<TargetControllerBlockEntity, Pair<BlockPos,Integer>> waypointDetails = new HashMap<>();
    private static final List<Block> woolIds = new ArrayList<>(64);
    private final Level level;

    private static SatelliteManager CLIENT_MANAGER;

    public static class SourceKey {
        public SatelliteBlockEntity satellite;
        public SatelliteControllerBlockEntity controller;

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
    private final boolean isServerSide;

    public SatelliteManager(Level level) {
        this.level = level;
        this.isServerSide = !level.isClientSide();
        this.satellites = new IntObjectHashMap<>(24);
        this.satellitePositions = new HashSet<>(24);
        this.displaySources = new HashMap<>();
    }

    public static void init(EventRegistrar reg) {

        reg.registerOnServerTick(TickType.ON_20_TICKS, SatelliteManager::onServer20Ticks);
        reg.registerOnTossItem(SatelliteManager::onTossSatellite);

        reg.registerOnSimpleMessage(MSG_ID_TARGET_POS, SatelliteManager::handleTargetPosMessage);

        reg.registerOnPlayerLogin(SatelliteManager::onPlayerJoined);
        //SatelliteDisplay
        SatelliteDisplay.init(reg);
    }


    public static SatelliteManager get(Level level) {
        if(level.isClientSide) {
            if(CLIENT_MANAGER == null)
                CLIENT_MANAGER = new SatelliteManager(level);
            return CLIENT_MANAGER;
        }
        return SatelliteMain.getManager(level);
    }

    public SatelliteBlockEntity get(int colorId) {
        return satellites.get(colorId);
    }

    public Set<SatelliteBlockEntity> getAllSatellites() {
        return new HashSet<>(satellitePositions);
    }

    public Set<SourceKey> getAllChannels() {
        return displaySources.keySet();
    }

    public TargetControllerBlockEntity getTargetController(int colorId, int targetColorId)
    {
        if(colorId<0 || targetColorId<0) return null;
        SatelliteBlockEntity satellite = get(colorId);
        if(satellite == null) return null;

        for(SourceKey key : displaySources.keySet()) {
            if(key.satellite == satellite) {
                SatelliteDisplay display = displaySources.get(key);
                if(display == null) continue;
                for(ISatelliteControllerBE be : display.getAllControllers(TargetControllerBlockEntity.class)) {
                    if(be instanceof TargetControllerBlockEntity targetController) {
                        if(targetController.getTargetColorId() == targetColorId) {
                            return targetController;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Removes any key with this satellite and adds new entry. Does not replace existing colorId entries.
     * @param colorId
     * @param be
     */
    public void put(int colorId, SatelliteBlockEntity be)
    {
        if(be == null) return;
        satellitePositions.add(be);
        if(colorId < 0 || satellites.containsKey(colorId)) return;

        List<Integer> colors = new ArrayList<>(satellites.keySet());
        for(int id : colors) {
            if(satellites.get(id) == be) {
                satellites.remove(id);
            }
        }
        satellites.putIfAbsent(colorId, be);
        be.setLevelChunk(getChunk(be.getLevel(), be.getBlockPos()));
    }

    public void remove(int colorId, SatelliteBlockEntity be) {
        if(be == null) return;
        satellitePositions.remove(be);
        if(colorId < 0) return;
        if(satellites.get(colorId) == be)
            satellites.remove(colorId);
    }

    public SatelliteDisplay generateSource(SatelliteBlockEntity satellite,
                                           SatelliteControllerBlockEntity controller)
    {
        anyControllerOn = true;
        SourceKey key = new SourceKey(satellite, controller);
        SatelliteDisplay satelliteDisplay = displaySources.get(key);
        if(satelliteDisplay != null) return satelliteDisplay;

        satelliteDisplay = new SatelliteDisplay(level, satellite, controller);
        satelliteDisplay.add(controller.getBlockPos(), controller);
        displaySources.put(key, satelliteDisplay);
        return satelliteDisplay;
    }

    public SatelliteDisplay getSource(SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller) {
        return displaySources.get(new SourceKey(satellite, controller));
    }

    public void putSource(SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller,
         SatelliteDisplay source) {
        displaySources.put(new SourceKey(satellite, controller), source);
    }

    public void removeSource(SatelliteBlockEntity satellite, SatelliteControllerBlockEntity controller) {
        displaySources.remove(new SourceKey(satellite, controller));
    }

    public int totalSatellites() { return satellites.size(); }

    public static int totalIds() {
        return woolIds.size();
    }

    private static void onTossSatellite(TossItemEvent event) {
        if(event.getPlayer().level().isClientSide) return;
        ItemStack stack =  event.getItemStack();
        if(!stack.is(ModBlocks.satelliteBlock.asItem())) return;
        stack.setCount(stack.getCount()-1);
        Player p = event.getPlayer();

        //Place satellite 1 bock below and in front of player
        BlockPos placePos = p.blockPosition().below().relative(p.getDirection());
        p.level().setBlock(placePos, ModBlocks.satelliteBlock.defaultBlockState(), 3);
        event.setCanceled(true);
    }

    public static Block getWool(int id) {
        return new ArrayList<>(woolIds).get(id % woolIds.size());
    }

    public static int getColorId(Block b) {
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


    public static LevelChunk getChunk(Level level, BlockPos pos) {
        return getChunk((ServerLevel) level, HBUtil.ChunkUtil.getChunkPos(pos));
    }

    public static LevelChunk getChunk(ServerLevel level, int chunkX, int chunkZ) {
        return getChunk(level, new ChunkPos(chunkX, chunkZ));
    }

    public static LevelChunk getChunk(ServerLevel level, ChunkPos pos)
    {
        if(level == null) return null;
        SatelliteManager manager = SatelliteMain.getManager(level);
        if(manager == null) return null;
        var chunkCache = manager.chunkCache;

        long posKey = HBUtil.ChunkUtil.getChunkPos1DMap(pos);
        CachedChunkInfo cachedInfo = chunkCache.get(posKey);
        if (cachedInfo != null) {
            cachedInfo.lifetime = 0;
            return cachedInfo.chunk;
        }

        // Try to get active chunk first
        String chunkId = HBUtil.ChunkUtil.getId(pos);
        ManagedChunk chunk = ManagedChunkUtility.getManagedChunk(level, chunkId);
        if (chunk != null) {
            chunkCache.put(posKey, new CachedChunkInfo(chunk.getLevelChunk(), false));
            return chunk.getLevelChunk();
        }

        // Try force loading
        HBUtil.ChunkUtil.forceLoadChunk(level, pos, Constants.MOD_ID);
        if (chunk != null) {
            LevelChunk levelChunk = chunk.getLevelChunk();
            chunkCache.put(posKey, new CachedChunkInfo(levelChunk, true));
            return levelChunk;
        }

        return null;
    }

    public static void flagChunkForUnload(Level level, ChunkPos pos) {

        if(level == null) return;
        SatelliteManager manager = SatelliteMain.getManager(level);
        if(manager == null) return;
        var chunkCache = manager.chunkCache;

        long posKey = HBUtil.ChunkUtil.getChunkPos1DMap(pos);
        CachedChunkInfo info = chunkCache.get(posKey);
        if (info != null) {
            info.lifetime = MAX_CHUNK_LIFETIME; // This will trigger unload on next tick
        }
    }

    public static final String MSG_ID_TARGET_POS = "satellite_target_pos";
    private static void handleTargetPosMessage(SimpleMessageEvent event) {
        JsonElement elem = JsonParser.parseString(event.getContent());

        if(elem == null || !elem.isJsonObject()) return;
        JsonObject json = elem.getAsJsonObject();
        int colorId = json.get("colorId").getAsInt();
        BlockPos targetPos = HBUtil.BlockUtil.stringToBlockPos(json.get("targetPos").getAsString());
        SatelliteManager manager = SatelliteMain.getManager(GeneralConfig.OVERWORLD);
        if(manager == null) return;
        SatelliteBlockEntity satellite = manager.get(colorId);
        if(satellite == null) return;
        satellite.launch(targetPos);
    }

    //Move satellite blockentity to target pos without disrupting any currently linked controllers
    public boolean moveSatellite(SatelliteBlockEntity be, BlockPos targetPos)
    {
        if(be == null || targetPos == null) return false;
        if(level == null || level.isClientSide) return false;

        BlockPos oldPos = be.getBlockPos();
        if(oldPos.equals(targetPos)) return false;

        ChunkPos targetChunk = new ChunkPos(targetPos);
        LevelChunk chunk = getChunk((ServerLevel) level, targetChunk);
        if(chunk == null) return false;

        int colorId = be.getColorId();
        remove(colorId, be);

        CompoundTag nbt = be.saveWithFullMetadata();
        BlockState state = level.getBlockState(oldPos);

        //While there is an existing satellite in the targetPos, move up 16 blocks
        while(level.getBlockEntity(targetPos) instanceof SatelliteBlockEntity) {
            targetPos = targetPos.above(1);
        }
        if(targetPos.getY() >= level.getMaxBuildHeight()) {
            CommonClass.MESSAGER.sendBottomActionHint(
                "Satellite " + colorId + " cannot be moved to target position, out of build height!");
            return true; //else it will keep retrying
        }

        level.removeBlock(oldPos, false);
        level.setBlock(targetPos, state, 3); // Flag 3 = notify + update

        BlockEntity newBE = level.getBlockEntity(targetPos);
        if(newBE instanceof SatelliteBlockEntity newSatellite) {

            newSatellite.load(nbt);
            newSatellite.setChanged();
            ((ServerLevel) level).getChunkSource().blockChanged(targetPos);
            put(colorId, newSatellite);
            return true;
        }

        return false;
    }

    public static final int START_BUFFER_TICKS = 100; //Satellites start working 200 ticks after server start
    private static int SATELLITE_SERVER_START_TICK = 0; //calculated tick when satellites will operate again
    private static Queue<Runnable> satelliteClientCommandQueue = new LinkedList<>();
    /**
     * Queues satellite actions to process until after start buffer has been filled
     * to make sure client-server actions are all processed
     */
    public static void queueSatelliteCommands(Runnable r) {
        satelliteClientCommandQueue.add(r);
        if(!bufferSatelliteStart()) r.run();
    }

    public static boolean bufferSatelliteStart() {
        return GeneralConfig.getInstance().getSessionTickCount() <= START_BUFFER_TICKS;
    }

    //** Events
    public static void onBeforeServerStart() {
        satelliteClientCommandQueue.clear();
        initWoolIds();
        SatelliteWeaponsManager.onBeforeServerStart();
    }

    public static void onWorldStop() {
        CLIENT_MANAGER = null;
    }

    public static void initWoolIds() {
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

        for(int i = 0; i < 16; i++) {
            WoolDustHelper.addDustColorFromWool(getWool(i), i);
        }
    }

    private static void onServer20Ticks(ServerTickEvent event)
    {
        //Check lifetime of sources
        Collection<SatelliteManager> managers = SatelliteMain.getAllManagers();
        for (SatelliteManager manager : managers) {
            manager.watchDisplaySourcesCache();
        }

        for (SatelliteManager manager : managers) {
            manager.watchChunkCache();
        }

    }

        private void watchChunkCache() {

            var iterator = chunkCache.values().iterator();
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

        private void watchDisplaySourcesCache() {

            Iterator<Map.Entry<SourceKey, SatelliteDisplay>> sourceIterator = displaySources.entrySet().iterator();
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
        }


    private static void onPlayerJoined(PlayerLoginEvent playerLoginEvent) {
        SatelliteWeaponsManager.sendAllActiveWaypoints(playerLoginEvent.getPlayer());
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

    // **MISC Statics**

    public static boolean isAnyControllerOn() {
        return  anyControllerOn;
    }

}
