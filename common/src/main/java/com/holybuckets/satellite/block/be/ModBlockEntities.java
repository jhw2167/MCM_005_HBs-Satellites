
package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.ModBlocks;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.block.BalmBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static DeferredObject<BlockEntityType<SatelliteBlockEntity>> satelliteBlockEntity;
    public static DeferredObject<BlockEntityType<SatelliteControllerBlockEntity>> satelliteControllerBlockEntity;
    public static DeferredObject<BlockEntityType<SatelliteDisplayBlockEntity>> satelliteDisplayBlockEntity;

    public static void initialize(BalmBlockEntities blockEntities)
    {
        satelliteBlockEntity =  blockEntities
            .registerBlockEntity( id("satellite_block_entity") , SatelliteBlockEntity::new,
                () -> new Block[]{ModBlocks.satelliteBlock} );

        satelliteControllerBlockEntity =  blockEntities.registerBlockEntity(
            id("satellite_controller_block_entity") , SatelliteControllerBlockEntity::new,
            () -> new Block[]{ModBlocks.satelliteControllerBlock} );

        satelliteDisplayBlockEntity =  blockEntities.registerBlockEntity(
            id("satellite_display_block_entity") , SatelliteDisplayBlockEntity::new,
            () -> new Block[]{ModBlocks.satelliteDisplayBlock} );

    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }
}
