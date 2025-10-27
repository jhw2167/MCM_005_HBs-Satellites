package com.holybuckets.satellite;


import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteDisplayBlockEntity;
import com.holybuckets.satellite.config.SatelliteConfig;
import com.holybuckets.satellite.core.SatelliteManager;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.LevelLoadingEvent;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Main instance of the mod, initialize this class statically via commonClass
 * This class will init all major Manager instances and events for the mod
 */
public class SatelliteMain {

    private static boolean DEV_MODE = false;
    public static SatelliteMain INSTANCE;
    public static ChiselBitsAPI chiselBitsApi;
    public static SatelliteConfig CONFIG;

    private static final Map<Level, SatelliteManager> MANAGERS = new HashMap<>();

    public SatelliteMain()
    {
        super();
        INSTANCE = this;
        INSTANCE.chiselBitsApi = (ChiselBitsAPI) Balm.platformProxy()
            .withForge("com.holybuckets.satellite.externalapi.ChiselBitsAPIForge")
            .build();

        //LoggerProject.logInit( "001000", this.getClass().getName() );
    }


    static void init(EventRegistrar reg)
    {

        /*
        Proxy for external APIs which are platform dependent
        this.portalApi = (PortalApi) Balm.platformProxy()
            .withFabric("com.holybuckets.challengetemple.externalapi.FabricPortalApi")
            .withForge("com.holybuckets.challengetemple.externalapi.ForgePortalApi")
            .build();
            */

        //Events
        SatelliteManager.init(reg);
        ChiselBitsAPI.init(reg);


        //register local events
        reg.registerOnBeforeServerStarted(INSTANCE::onServerStarting, EventPriority.Highest);
        reg.registerOnLevelLoad(INSTANCE::onLoadLevel, EventPriority.Normal);
        reg.registerOnServerStopped(INSTANCE::onServerStopped, EventPriority.Lowest);

    }

    public static void loadConfig() {
        CONFIG = Balm.getConfig().getActiveConfig(SatelliteConfig.class);
    }


    //** EVENTS



    //** Events
    private void onServerStarting(ServerStartingEvent e) {
        //this.DEV_MODE = CONFIG.devMode;
        this.DEV_MODE = false;
        loadConfig();
        SatelliteManager.initWoolIds();

        //Set Configs Entity blocks
        SatelliteControllerBlockEntity.PATH_REFRESH_TICKS = CONFIG.refreshRates.controllerPathRefreshRate;
        SatelliteControllerBlockEntity.UI_REFRESH_TICKS = CONFIG.refreshRates.controllerUIRefreshRate;
        SatelliteControllerBlockEntity.ENTITY_REFRESH_TICKS = CONFIG.refreshRates.entityRefreshRate;

        SatelliteDisplayBlockEntity.REFRESH_RATE = CONFIG.refreshRates.displayRefreshRate;
        SatelliteDisplayBlockEntity.PLAYER_REFRESH_RATE = CONFIG.refreshRates.displayPlayerRefreshRate;
    }

    private void onLoadLevel(LevelLoadingEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        MANAGERS.putIfAbsent(level, new SatelliteManager(level));
    }

    private void onServerStopped(ServerStoppedEvent e) {
        MANAGERS.values().forEach(SatelliteManager::shutdown);
        MANAGERS.clear();
    }

    public static SatelliteManager getManager(Level level) {
        if (level.isClientSide()) return null;
        return MANAGERS.computeIfAbsent(level, SatelliteManager::new);
    }

    public static Collection<SatelliteManager> getAllManagers() {
        return MANAGERS.values();
    }


}
