package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Quick flame streak that traces a sword swing.
 */
public class FireSlashParticle extends AbstractFireParticle {

    private static final float DECAY_START_FRACTION = 0.75F;

    protected FireSlashParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.friction = 0.9F;
        this.quadSize = 0.4F + this.random.nextFloat() * 0.1F;
        this.lifetime = 8;
    }

    @Override
    public void tick() {
        super.tick();
        float lifeFraction = (float) this.age / (float) this.lifetime;
        if (lifeFraction > DECAY_START_FRACTION) {
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
            return new FireSlashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
