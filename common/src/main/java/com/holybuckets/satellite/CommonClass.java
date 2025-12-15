package com.holybuckets.satellite;

import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.console.IMessager;
import com.holybuckets.foundation.event.BalmEventRegister;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.ModBlockEntities;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.command.CommandList;
import com.holybuckets.satellite.config.ModConfig;
import com.holybuckets.satellite.item.ModItems;
import com.holybuckets.satellite.menu.ModMenus;
import com.holybuckets.satellite.networking.ModNetworking;
import com.holybuckets.satellite.particle.ModParticles;
import com.holybuckets.satellite.config.SatelliteConfig;
import com.holybuckets.satellite.platform.Services;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class CommonClass implements CommonProxy {

    public static boolean isInitialized = false;
    public static IMessager MESSAGER;
    public static void init()
    {
        if (isInitialized)
            return;

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", com.holybuckets.satellite.platform.Services.PLATFORM.getPlatformName(), com.holybuckets.satellite.platform.Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        //Initialize Foundations
        com.holybuckets.foundation.FoundationInitializers.commonInitialize();
        MESSAGER = com.holybuckets.foundation.CommonClass.MESSAGER;

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

        CommandList.register();
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


    //** Utility

    public static boolean isViewingHoloBlock(Level level, BlockHitResult hitResult) {
        return SatelliteMain.chiselBitsApi.isViewingHoloBlock(level, hitResult);
    }

    public static boolean isViewingHoloBit(Level level, BlockHitResult hitResult, Vec3 bitOffset) {
        return SatelliteMain.chiselBitsApi.isViewingHoloBit(level, hitResult, bitOffset);
    }

    private static double VW_RANGE = 2 * 0.0625d;
    public static BlockHitResult getAnyHitResult(Level level, Player player, double startReach)
    {
        BlockHitResult hitResult1 = (BlockHitResult) player.pick(startReach, 0.5f, true);
        if (hitResult1.getType() != HitResult.Type.BLOCK) return null;
        BlockHitResult hitResult2 = (BlockHitResult) player.pick(startReach*2, 0.5f, true);
        BlockHitResult bufferedResult = new BlockHitResult(
            hitResult1.getLocation().add(0, -VW_RANGE, 0),
            hitResult1.getDirection(),
            hitResult1.getBlockPos(),
            hitResult1.isInside()
        );
        if( isViewingHoloBlock(level, hitResult1) ) {}
        else if( isViewingHoloBlock(level, hitResult2) ) { hitResult1 = hitResult2; }
        else if( isViewingHoloBlock(level, bufferedResult) ) { hitResult1 = bufferedResult; }
        else return null;
        return hitResult1;
    }

    public static void clientSideActions(Level level, Object obj) {
        if(obj == null) return;
        if(!level.isClientSide()) return;
        ((CommonProxy) Balm.sidedProxy( "com.holybuckets.satellite.CommonClass", "com.holybuckets.satellite.client.CommonClassClient")
            .get()).openScreen(obj);
    }
}
