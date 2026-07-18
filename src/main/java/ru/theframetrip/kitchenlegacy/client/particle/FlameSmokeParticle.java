package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class FlameSmokeParticle extends TextureSheetParticle {

    private static final float DECAY_START_FRACTION = 0.5F;

    private final SpriteSet sprites;
    private final float growth;

    protected FlameSmokeParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.gravity = 0.0F;
        this.friction = 0.96F;
        this.hasPhysics = false;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.05F;
        this.growth = 1.02F + this.random.nextFloat() * 0.01F;
        this.lifetime = 30 + this.random.nextInt(15);
        this.yd = ySpeed + 0.01F + this.random.nextFloat() * 0.01F;
        this.alpha = 0.6F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.quadSize *= this.growth;

        float lifeFraction = (float) this.age / (float) this.lifetime;
        if (lifeFraction > DECAY_START_FRACTION) {
            float decayFraction = (lifeFraction - DECAY_START_FRACTION) / (1.0F - DECAY_START_FRACTION);
            this.alpha = 0.6F * (1.0F - decayFraction);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            return new FlameSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
