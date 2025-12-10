package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.block.be.ModBlockEntities;
import net.blay09.mods.balm.api.client.rendering.BalmRenderers;

public class ModRenderers {
    public static void clientInitialize(BalmRenderers renderers)
    {

        renderers.registerBlockEntityRenderer(
            ModBlockEntities.satelliteBlockEntity::get,
            SatelliteRenderer::new
        );

        renderers.registerBlockEntityRenderer(
            ModBlockEntities.satelliteControllerBlockEntity::get,
            SatelliteControllerRenderer::new
        );
        
        renderers.registerBlockEntityRenderer(
            ModBlockEntities.targetControllerBlockEntity::get,
            TargetControllerRenderer::new
        );

        renderers.registerBlockEntityRenderer(
            ModBlockEntities.upgradeControllerBlockEntity::get,
            UpgradeControllerRenderer::new
        );
    }
}
