package com.holybuckets.satellite.particles;

import com.holybuckets.satellite.Constants;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.particles.BalmParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

public class ModParticles {
    public static SimpleParticleType HOLO_SPARK;
    public static SimpleParticleType HOLO_TRACE;
    
    public static void initialize(BalmParticles particles) {
        particles.register(() -> HOLO_SPARK = new SimpleParticleType(false), id("holo_spark"));
        particles.register(() -> HOLO_TRACE = new SimpleParticleType(false), id("holo_trace"));
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }
}
