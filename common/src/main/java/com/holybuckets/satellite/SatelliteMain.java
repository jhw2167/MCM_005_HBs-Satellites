package com.holybuckets.satellite;


import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.config.SatelliteConfig;
import com.holybuckets.satellite.core.SatelliteManager;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;

/**
 * Main instance of the mod, initialize this class statically via commonClass
 * This class will init all major Manager instances and events for the mod
 */
public class SatelliteMain {
    public static SatelliteManager MANAGER;
    private static boolean DEV_MODE = false;;
    public static SatelliteMain INSTANCE;
    public static ChiselBitsAPI chiselBitsApi;
    public static SatelliteConfig CONFIG;

    public SatelliteMain()
    {
        super();
        INSTANCE = this;
        INSTANCE.chiselBitsApi = (ChiselBitsAPI) Balm.platformProxy()
            .withForge("com.holybuckets.satellite.externalapi.ChiselBitsAPIForge")
            .build();

        MANAGER = new SatelliteManager();
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
    }


}
