package com.holybuckets.satellite.client;

import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.particle.ModParticles;
import net.blay09.mods.balm.api.client.rendering.BalmRenderers;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.BubblePopParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;

public class ModRenderers {

    //public static ModelLayerLocation someModel;

    public static void clientInitialize(BalmRenderers renderers) {

        renderers.setBlockRenderType(() -> ModBlocks.holoBaseBlock, RenderType.cutout() );
        renderers.setBlockRenderType(() -> ModBlocks.holoBaseBlock, RenderType.translucent());
        renderers.setBlockRenderType(() -> ModBlocks.holoDarkBlock, RenderType.translucent());
        //waystoneModel = renderers.registerModel(new ResourceLocation(Waystones.MOD_ID, "waystone"), () -> WaystoneModel.createLayer(CubeDeformation.NONE));
        //renderers.setBlockRenderType(() -> ModBlocks.stoneBrickBlockEntity, RenderType.cutout());

        //Particle Rendering
        //renderers.registerParticleProvider(ModParticles.basePingId, () -> ModParticles.basePing, BubblePopParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.basePingId, () -> ModParticles.basePing, EndRodParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.redPingId, () -> ModParticles.redPing, EndRodParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.bluePingId, () -> ModParticles.bluePing, EndRodParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.greenPingId, () -> ModParticles.greenPing, EndRodParticle.Provider::new );
        renderers.registerParticleProvider(ModParticles.orangePingId, () -> ModParticles.orangePing, EndRodParticle.Provider::new );
            
        renderers.registerParticleProvider(ModParticles.hoverOrangeId, () -> ModParticles.hoverOrange, EndRodParticle.Provider::new );
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
