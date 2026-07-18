package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Sharp flash on landing a hit with a fire sword.
 */
public class FireHitParticle extends AbstractFireParticle {

    private static final float DECAY_START_FRACTION = 0.4F;

    protected FireHitParticle(ClientLevel level, double x, double y, double z,
                               double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.friction = 0.85F;
        this.quadSize = 0.35F + this.random.nextFloat() * 0.1F;
        this.lifetime = 8;
        this.alpha = 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        this.quadSize *= 1.03F;
        float lifeFraction = (float) this.age / (float) this.lifetime;
        if (lifeFraction > DECAY_START_FRACTION) {
            this.alpha = 1.0F - (lifeFraction - DECAY_START_FRACTION) / (1.0F - DECAY_START_FRACTION);
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
            return new FireHitParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
