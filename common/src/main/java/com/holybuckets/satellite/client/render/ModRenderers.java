package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.ModBlockEntities;
import com.holybuckets.satellite.particle.ModParticles;
import com.holybuckets.satellite.particle.StaticGlowParticle;
import net.blay09.mods.balm.api.client.rendering.BalmRenderers;
import net.minecraft.client.renderer.RenderType;

public class ModRenderers {
    public static void clientInitialize(BalmRenderers renderers)
    {

        renderers.setBlockRenderType(() -> ModBlocks.satelliteBlock, RenderType.cutout());

        //Particle Rendering
        //renderers.registerParticleProvider(ModParticles.basePingId, () -> ModParticles.basePing, BubblePopParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.basePingId, () -> ModParticles.basePing, StaticGlowParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.redPingId, () -> ModParticles.redPing, StaticGlowParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.bluePingId, () -> ModParticles.bluePing, StaticGlowParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.greenPingId, () -> ModParticles.greenPing, StaticGlowParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.orangePingId, () -> ModParticles.orangePing, StaticGlowParticle.Provider::new );

        //Block Entity Renderers
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
            ModBlockEntities.targetReceiverBlockEntity::get,
            TargetReceiverRenderer::new
        );


        renderers.registerBlockEntityRenderer(
            ModBlockEntities.upgradeControllerBlockEntity::get,
            UpgradeControllerRenderer::new
        );
    }
}
