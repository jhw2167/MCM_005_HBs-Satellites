package com.holybuckets.satellite.item;


import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.ModBlocks;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.item.BalmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ModItems {
    public static DeferredObject<CreativeModeTab> creativeModeTab;

    // Upgrade items
    public static SatelliteItemUpgrade multilinkUpgrade;
    public static SatelliteItemUpgrade oreScannerUpgrade;
    public static SatelliteItemUpgrade rangeUpgrade;
    public static SatelliteItemUpgrade depthUpgrade;
    public static SatelliteItemUpgrade entityScannerUpgrade;
    public static SatelliteItemUpgrade playerScannerUpgrade;

    public static void initialize(BalmItems items) {
        creativeModeTab = items.registerCreativeModeTab(id(Constants.MOD_ID), () -> new ItemStack(ModBlocks.satelliteBlock));

        // Register upgrade items with random dye colors
        items.registerItem(() -> multilinkUpgrade = new SatelliteItemUpgrade(DyeColor.PURPLE), id("multilink_upgrade"));
        items.registerItem(() -> oreScannerUpgrade = new SatelliteItemUpgrade(DyeColor.GREEN), id("ore_scanner_upgrade"));
        items.registerItem(() -> rangeUpgrade = new SatelliteItemUpgrade(DyeColor.BLUE), id("range_upgrade"));
        items.registerItem(() -> depthUpgrade = new SatelliteItemUpgrade(DyeColor.BROWN), id("depth_upgrade"));
        items.registerItem(() -> entityScannerUpgrade = new SatelliteItemUpgrade(DyeColor.RED), id("entity_scanner_upgrade"));
        items.registerItem(() -> playerScannerUpgrade = new SatelliteItemUpgrade(DyeColor.YELLOW), id("player_scanner_upgrade"));
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
