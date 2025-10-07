package com.holybuckets.satellite.block;

import com.holybuckets.satellite.Constants;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.block.BalmBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static Block templateBlock;
    public static Block satelliteBlock;
    public static Block satelliteControllerBlock;
    public static Block satelliteDisplayBlock;
    public static Block holoBaseBlock;
    //public static Block[] scopedSharestones = new SharestoneBlock[DyeColor.values().length];

    public static void initialize(BalmBlocks blocks) {
        //blocks.register(() -> templateBlock = new EmptyBlock(defaultProperties()), () -> itemBlock(templateBlock), id("template_block"));
        blocks.register(() -> holoBaseBlock = new HoloBaseBlock(), () -> itemBlock(holoBaseBlock), id("holo_base_block"));
        blocks.register(() -> satelliteBlock = new SatelliteBlock(), () -> itemBlock(satelliteBlock), id("satellite_block"));
        blocks.register(() -> satelliteControllerBlock = new SatelliteControllerBlock(), () -> itemBlock(satelliteControllerBlock), id("satellite_controller_block"));
        blocks.register(() -> satelliteDisplayBlock = new SatelliteDisplayBlock(), () -> itemBlock(satelliteDisplayBlock), id("satellite_display_block"));

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

    private static BlockBehaviour.Properties defaultProperties() {
        return Balm.getBlocks().blockProperties().sound(SoundType.STONE).strength(5f, 2000f);
    }
}
