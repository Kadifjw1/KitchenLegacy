package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Smoke that drifts upward, grows from ~0.12 to ~0.30 blocks and fades out.
 * Spawn 1-3 per burst from the calling code.
 */
public class FlameSmokeParticle extends TextureSheetParticle {

    private static final float START_SIZE = 0.12F;
    private static final float END_SIZE = 0.30F;
    private static final float START_ALPHA = 0.6F;
    private static final float DECAY_START_FRACTION = 0.5F;

    private final SpriteSet sprites;
    private final float growth;

    protected FlameSmokeParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.gravity = 0.0F;
        this.friction = 0.98F;
        this.hasPhysics = false;
        this.quadSize = START_SIZE;
        this.lifetime = 18 + this.random.nextInt(13);
        this.growth = (float) Math.pow(END_SIZE / START_SIZE, 1.0 / this.lifetime);
        this.yd = ySpeed + 0.008F + this.random.nextFloat() * 0.008F;
        this.alpha = START_ALPHA;
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
            this.alpha = START_ALPHA * (1.0F - decayFraction);
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
