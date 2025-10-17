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
    
    public static ResourceLocation redPingId = id("red_ping");
    public static SimpleParticleType redPing;
    public static ResourceLocation bluePingId = id("blue_ping");
    public static SimpleParticleType bluePing;
    public static ResourceLocation greenPingId = id("green_ping");
    public static SimpleParticleType greenPing;
    public static ResourceLocation orangePingId = id("orange_ping");
    public static SimpleParticleType orangePing;

    public static void initialize(BalmParticles particles) {
        particles.registerParticle(
            (r) -> basePing = particles.createSimple(true), basePingId );
        particles.registerParticle(
            (r) -> hoverOrange = particles.createSimple(true), hoverOrangeId = id("hover_orange") );
        particles.registerParticle(
            (r) -> redPing = particles.createSimple(true), redPingId);
        particles.registerParticle(
            (r) -> bluePing = particles.createSimple(true), bluePingId);
        particles.registerParticle(
            (r) -> greenPing = particles.createSimple(true), greenPingId);
        particles.registerParticle(
            (r) -> orangePing = particles.createSimple(true), orangePingId);
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }
}
