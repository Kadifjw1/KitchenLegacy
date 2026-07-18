package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Large stationary flash used for abilities and charged strikes. Grows by
 * roughly 20-40% over its lifetime before fading.
 */
public class FireBurstParticle extends AbstractFireParticle {

    private static final float DECAY_START_FRACTION = 0.6F;

    private final float growth;

    protected FireBurstParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.quadSize = 1.5F + this.random.nextFloat() * 1.5F;
        this.lifetime = 10 + this.random.nextInt(5);
        float totalGrowth = 1.2F + this.random.nextFloat() * 0.2F;
        this.growth = (float) Math.pow(totalGrowth, 1.0 / this.lifetime);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    @Override
    protected void updateSprite() {
        this.setSpriteFromAge(this.sprites);
        this.quadSize *= this.growth;
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
            return new FireBurstParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
