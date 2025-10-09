package com.holybuckets.satellite.particle;

import com.holybuckets.satellite.Constants;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.particle.BalmParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

public class ModParticles {
    public static ResourceLocation basePingId = id("base_ping");
    public static SimpleParticleType basePing;
    public static ResourceLocation hoverOrangeId;
    public static SimpleParticleType hoverOrange;

    public static void initialize(BalmParticles particles) {
        particles.registerParticle(
            (r) -> basePing = particles.createSimple(true), basePingId );
        particles.registerParticle(
            (r) -> hoverOrange = particles.createSimple(true), hoverOrangeId = id("hover_orange") );
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }
}
