package com.holybuckets.satellite.item;


import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.ModBlocks;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.item.BalmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {
    public static DeferredObject<CreativeModeTab> creativeModeTab;

    // Upgrade items
    public static DeferredObject<UpdateSatelliteItem> multilinkUpgrade;
    public static DeferredObject<UpdateSatelliteItem> oreScannerUpgrade;
    public static DeferredObject<UpdateSatelliteItem> rangeUpgrade;
    public static DeferredObject<UpdateSatelliteItem> depthUpgrade;
    public static DeferredObject<UpdateSatelliteItem> entityScannerUpgrade;
    public static DeferredObject<UpdateSatelliteItem> playerScannerUpgrade;

    public static void initialize(BalmItems items) {
        creativeModeTab = items.registerCreativeModeTab(id(Constants.MOD_ID), () -> new ItemStack(ModBlocks.satelliteBlock));

        // Register upgrade items with random dye colors
        multilinkUpgrade = items.registerItem(id("multilink_upgrade"), () -> new UpdateSatelliteItem(DyeColor.PURPLE));
        oreScannerUpgrade = items.registerItem(id("ore_scanner_upgrade"), () -> new UpdateSatelliteItem(DyeColor.ORANGE));
        rangeUpgrade = items.registerItem(id("range_upgrade"), () -> new UpdateSatelliteItem(DyeColor.GREEN));
        depthUpgrade = items.registerItem(id("depth_upgrade"), () -> new UpdateSatelliteItem(DyeColor.BLUE));
        entityScannerUpgrade = items.registerItem(id("entity_scanner_upgrade"), () -> new UpdateSatelliteItem(DyeColor.RED));
        playerScannerUpgrade = items.registerItem(id("player_scanner_upgrade"), () -> new UpdateSatelliteItem(DyeColor.YELLOW));
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
