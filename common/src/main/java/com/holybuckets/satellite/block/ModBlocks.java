package com.holybuckets.satellite.block;

import com.holybuckets.satellite.Constants;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.block.BalmBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class ModBlocks {

    public static Block templateBlock;
    public static Block satelliteBlock;
    public static Block satelliteControllerBlock;
    public static Block satelliteDisplayBlock;
    public static Block holoBaseBlock;
    public static Block holoLightBlock;
    public static Block holoDarkBlock;
    public static Block holoAirBlock;
    public static Block emptyControllerBlock;
    public static Block heightControllerBlock;
    public static Block positionControllerBlock;
    public static Block targetControllerBlock;
    public static Block targetReceiverBlock;
    public static Block upgradeControllerBlock;
    //public static Block[] scopedSharestones = new SharestoneBlock[DyeColor.values().length];

    public static void initialize(BalmBlocks blocks) {
        //blocks.register(() -> templateBlock = new EmptyBlock(defaultProperties()), () -> itemBlock(templateBlock), id("template_block"));
        blocks.register(() -> holoBaseBlock = new HoloBlock(HoloBaseBlock.HOLO_BASE_PROPERTIES), () -> itemBlock(holoBaseBlock), id("holo_base_block"));
        blocks.register(() -> holoLightBlock = new HoloBlock(HoloBaseBlock.HOLO_BASE_PROPERTIES), () -> itemBlock(holoBaseBlock), id("holo_light_block"));
        blocks.register(() -> holoDarkBlock = new HoloBlock(HoloBaseBlock.HOLO_BASE_PROPERTIES), () -> itemBlock(holoDarkBlock), id("holo_dark_block"));
        blocks.register(() -> holoAirBlock = new HoloBaseBlock(HoloBaseBlock.HOLO_AIR_PROPERTIES), () -> itemBlock(holoAirBlock), id("holo_air_block"));
        blocks.register(() -> satelliteBlock = new SatelliteBlock(), () -> itemBlock(satelliteBlock), id("satellite_block"));
        blocks.register(() -> satelliteControllerBlock = new SatelliteControllerBlock(), () -> itemBlock(satelliteControllerBlock), id("satellite_controller_block"));
        blocks.register(() -> satelliteDisplayBlock = new SatelliteDisplayBlock(), () -> itemBlock(satelliteDisplayBlock), id("satellite_display_block"));
        blocks.register(() -> emptyControllerBlock = new EmptyControllerBlock(), () -> itemBlock(emptyControllerBlock), id("empty_controller_block"));
        blocks.register(() -> heightControllerBlock = new HeightControllerBlock(), ()-> itemBlock(heightControllerBlock), id("height_controller_block"));
        blocks.register(() -> positionControllerBlock = new PositionControllerBlock(), () -> itemBlock(positionControllerBlock), id("position_controller_block"));
        blocks.register(() -> targetControllerBlock = new TargetControllerBlock(), () -> itemBlock(targetControllerBlock), id("target_controller_block"));
        blocks.register(() -> targetReceiverBlock = new TargetReceiverBlock(), () -> itemBlock(targetReceiverBlock), id("target_receiver_block"));
        blocks.register(() -> upgradeControllerBlock = new UpgradeControllerBlock(), () -> itemBlock(upgradeControllerBlock), id("upgrade_controller_block"));

        /*
        DyeColor[] colors = DyeColor.values();
        for (DyeColor color : colors) {
            blocks.register(() -> scopedSharestones[color.ordinal()] = new SharestoneBlock(defaultProperties(), color), () -> itemBlock(scopedSharestones[color.ordinal()]), id(color.getSerializedName() + "_sharestone"));
        }
        */

    }

    private static BlockItem itemBlock(Block block) {
        return new BlockItem(block, Balm.getItems().itemProperties());
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
