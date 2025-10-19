package com.holybuckets.satellite;

import com.holybuckets.foundation.event.BalmEventRegister;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.ModBlockEntities;
import com.holybuckets.satellite.client.ModRenderers;
import com.holybuckets.satellite.config.ModConfig;
import com.holybuckets.satellite.config.SatelliteConfig;
import com.holybuckets.satellite.item.ModItems;
import com.holybuckets.satellite.menu.ModMenus;
import com.holybuckets.satellite.networking.ModNetworking;
import com.holybuckets.satellite.particle.ModParticles;
import com.holybuckets.satellite.platform.Services;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;


public class CommonClass {

    public static boolean isInitialized = false;
    public static void init()
    {
        if (isInitialized)
            return;

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", com.holybuckets.satellite.platform.Services.PLATFORM.getPlatformName(), com.holybuckets.satellite.platform.Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        //Initialize Foundations
        com.holybuckets.foundation.FoundationInitializers.commonInitialize();

        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
            Constants.LOG.info("Hello to " + Constants.MOD_NAME + "!");
        }

        //RegisterConfigs
        Balm.getConfig().registerConfig(SatelliteConfig.class);

        SatelliteMain.INSTANCE = new SatelliteMain();
        EventRegistrar reg = EventRegistrar.getInstance();
        SatelliteMain.init(reg);
        ModConfig.init(reg);

        BalmEventRegister.registerEvents();
        BalmEventRegister.registerCommands();
        ModBlocks.initialize(Balm.getBlocks());
        ModBlockEntities.initialize(Balm.getBlockEntities());
        ModItems.initialize(Balm.getItems());
        ModMenus.initialize(Balm.getMenus());
        ModParticles.initialize(Balm.getParticles());
        ModNetworking.initialize();
        
        isInitialized = true;
    }

    /**
     * Description: Run sample tests methods
     */
    public static void sample()
    {

    }

    public static void initClient() {
        ModRenderers.clientInitialize(BalmClient.getRenderers());
    }
}
