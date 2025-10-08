package com.holybuckets.satellite.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class BasePingParticle extends BaseAshSmokeParticle {
    
    protected BasePingParticle(ClientLevel level, double x, double y, double z, 
                              double xSpeed, double ySpeed, double zSpeed, 
                              float scale, float gravity, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, scale, gravity, sprites);
        
        // Customize particle behavior
        this.lifetime = 20;  // Duration in ticks
        this.gravity = gravity;
        this.friction = 0.96F;
        
        // Pick a random sprite from the sprite sheet
        this.pickSprite(sprites);
        
        // Set initial color (light blue)
        this.setColor(0.5F, 0.8F, 1.0F);
        
        // Set alpha (transparency)
        this.alpha = 0.8F;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                     double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed) {
            return new BasePingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, 
                                      1.0F, 0.0F, this.sprites);
        }
    }
}
