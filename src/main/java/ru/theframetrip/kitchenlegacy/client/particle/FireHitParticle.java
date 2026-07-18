package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import ru.theframetrip.kitchenlegacy.registry.ModParticleTypes;

/**
 * Sharp, almost stationary flash at the exact point of impact. Also spawns
 * a handful of flame_spark particles for extra punch.
 */
public class FireHitParticle extends AbstractFireParticle {

    private static final float DECAY_START_FRACTION = 0.4F;

    protected FireHitParticle(ClientLevel level, double x, double y, double z,
                               double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.friction = 0.85F;
        this.quadSize = 0.5F + this.random.nextFloat() * 0.5F;
        this.lifetime = 6 + this.random.nextInt(3);
        this.alpha = 1.0F;
        this.xd = xSpeed * 0.2;
        this.yd = ySpeed * 0.2;
        this.zd = zSpeed * 0.2;

        int sparkCount = 4 + this.random.nextInt(7);
        for (int i = 0; i < sparkCount; i++) {
            double vx = (this.random.nextFloat() - 0.5F) * 0.3F;
            double vy = this.random.nextFloat() * 0.25F;
            double vz = (this.random.nextFloat() - 0.5F) * 0.3F;
            level.addParticle(ModParticleTypes.FLAME_SPARK.get(), x, y, z, vx, vy, vz);
        }
    }

    @Override
    protected void updateSprite() {
        this.setSpriteFromAge(this.sprites);
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
