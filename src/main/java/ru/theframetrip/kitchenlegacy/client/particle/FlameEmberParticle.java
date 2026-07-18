package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class FlameEmberParticle extends TextureSheetParticle {

    private static final float FADE_IN_FRACTION = 0.15F;
    private static final float DECAY_START_FRACTION = 0.55F;

    private final SpriteSet sprites;

    protected FlameEmberParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.gravity = 0.35F;
        this.friction = 0.92F;
        this.hasPhysics = true;
        this.quadSize = 0.08F + this.random.nextFloat() * 0.06F;
        this.lifetime = 15 + this.random.nextInt(10);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        float lifeFraction = (float) this.age / (float) this.lifetime;
        if (lifeFraction < FADE_IN_FRACTION) {
            this.alpha = lifeFraction / FADE_IN_FRACTION;
        } else if (lifeFraction > DECAY_START_FRACTION) {
            float decayFraction = (lifeFraction - DECAY_START_FRACTION) / (1.0F - DECAY_START_FRACTION);
            this.alpha = 1.0F - decayFraction;
        } else {
            this.alpha = 1.0F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            return new FlameEmberParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
