package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Rising flame pillar that builds up intensity, used while charging a special attack.
 */
public class FireChargeParticle extends AbstractFireParticle {

    protected FireChargeParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.friction = 0.96F;
        this.quadSize = 0.3F + this.random.nextFloat() * 0.08F;
        this.lifetime = 20 + this.random.nextInt(6);
        this.yd = ySpeed + 0.01F;
        this.alpha = 0.2F;
    }

    @Override
    public void tick() {
        super.tick();
        float lifeFraction = (float) this.age / (float) this.lifetime;
        this.alpha = 0.2F + 0.8F * Math.min(lifeFraction * 1.5F, 1.0F);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            return new FireChargeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
