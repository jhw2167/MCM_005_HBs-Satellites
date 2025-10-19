package com.holybuckets.satellite.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StaticGlowParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected StaticGlowParticle(ClientLevel level, double x, double y, double z,
                                 SpriteSet spriteSet) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        this.sprites = spriteSet;

        // No velocity - stays in place
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;

        // No gravity
        this.gravity = 0.0F;

        // No friction
        this.friction = 1.0F;

        // Particle size (adjust as needed)
        this.quadSize = 0.15F;

        // Lifetime - adjust for fade duration
        this.lifetime = 10;

        // Start with alpha = 0 for fade in
        this.alpha = 0.0F;

        // Color (white by default, can customize)
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        // Don't collide with blocks
        this.hasPhysics = false;

        // Pick initial sprite
        this.setSpriteFromAge(spriteSet);
    }

    private static final int DURATION_FADE_IN = 1;
    private static final int DURATION_FADE_OUT = 5;
    private static final float TEMPO_FADE = 1.0F / DURATION_FADE_IN;
    @Override
    public void tick() {
        // Store previous position
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Age the particle
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Handle fade in/out
        if (this.age < DURATION_FADE_IN) {
            this.alpha += TEMPO_FADE;
        } else if (this.age > this.lifetime - DURATION_FADE_OUT) {
            this.alpha -= TEMPO_FADE;
        } else {
            // Fully visible in between
            this.alpha = 1.0F;
        }

        // Optional: animate through sprite frames
        this.setSpriteFromAge(sprites);

        // NO MOVEMENT - particle stays static
        // (velocity is already 0, but explicitly ensure no drift)
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Use translucent for fade effects
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        // Always render at full brightness (glowing effect)
        return 15728880; // 0xF000F0
    }

    // Particle Provider/Factory
    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new StaticGlowParticle(level, x, y, z, this.sprites);
        }
    }
}

// Alternative: Pulsing particle (brightness pulses instead of fade in/out)
@OnlyIn(Dist.CLIENT)
class PulsingStaticParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected PulsingStaticParticle(ClientLevel level, double x, double y, double z,
                                    SpriteSet spriteSet) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        this.sprites = spriteSet;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.quadSize = 0.15F;
        this.lifetime = 80; // Longer for pulsing effect
        this.hasPhysics = false;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Sine wave pulsing (smooth in/out)
        float pulsePhase = (float) this.age / (float) this.lifetime * (float) Math.PI * 2.0F;
        this.alpha = (float) (Math.sin(pulsePhase) * 0.5 + 0.5); // 0.0 to 1.0 sine wave

        // Scale pulsing (optional - makes particle grow/shrink)
        // this.quadSize = 0.15F * (0.8F + alpha * 0.4F);

        this.setSpriteFromAge(sprites);

        // Static position
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new PulsingStaticParticle(level, x, y, z, this.sprites);
        }
    }
}

// 3D Billboard particle - rotates to face camera for 3D appearance
@OnlyIn(Dist.CLIENT)
class Static3DBillboardParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected Static3DBillboardParticle(ClientLevel level, double x, double y, double z,
                                        SpriteSet spriteSet) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        this.sprites = spriteSet;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.quadSize = 0.2F;
        this.lifetime = 40;
        this.hasPhysics = false;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Fade in/out
        float ageRatio = (float) this.age / (float) this.lifetime;

        if (ageRatio < 0.25F) {
            this.alpha = ageRatio / 0.25F;
        } else if (ageRatio < 0.75F) {
            this.alpha = 1.0F;
        } else {
            this.alpha = (1.0F - ageRatio) / 0.25F;
        }

        // Slow rotation for 3D effect (optional)
        this.oRoll = this.roll;
        this.roll += 0.02F;

        this.setSpriteFromAge(sprites);

        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new Static3DBillboardParticle(level, x, y, z, this.sprites);
        }
    }
}