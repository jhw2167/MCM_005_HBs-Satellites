package com.holybuckets.satellite.client;

import com.holybuckets.satellite.block.be.ModBlockEntities;
import net.blay09.mods.balm.api.client.rendering.BalmRenderers;

public class ModRenderers {
    public static void clientInitialize(BalmRenderers renderers) {
        renderers.registerBlockEntityRenderer(
            ModBlockEntities.satelliteControllerBlockEntity::get,
            SatelliteControllerRenderer::new
        );
        
        renderers.registerBlockEntityRenderer(
            ModBlockEntities.targetControllerBlockEntity::get,
            TargetControllerRenderer::new
        );
    }
}
