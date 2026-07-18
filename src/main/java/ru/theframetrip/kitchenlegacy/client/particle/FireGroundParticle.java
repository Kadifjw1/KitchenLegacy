package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Lingering flame patch that clings to the ground after an impact.
 */
public class FireGroundParticle extends AbstractFireParticle {

    private static final float FADE_IN_FRACTION = 0.1F;
    private static final float DECAY_START_FRACTION = 0.7F;

    protected FireGroundParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.friction = 0.98F;
        this.quadSize = 0.3F + this.random.nextFloat() * 0.1F;
        this.lifetime = 25 + this.random.nextInt(10);
        this.yd = 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        float lifeFraction = (float) this.age / (float) this.lifetime;
        if (lifeFraction < FADE_IN_FRACTION) {
            this.alpha = lifeFraction / FADE_IN_FRACTION;
        } else if (lifeFraction > DECAY_START_FRACTION) {
            this.alpha = 1.0F - (lifeFraction - DECAY_START_FRACTION) / (1.0F - DECAY_START_FRACTION);
        } else {
            this.alpha = 1.0F;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            return new FireGroundParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
