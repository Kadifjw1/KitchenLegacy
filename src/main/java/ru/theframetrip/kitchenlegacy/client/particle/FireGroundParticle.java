package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Flame patch fixed to the ground after an impact. Plays frames 0-2 as an
 * ignition, loops frames 3-7 for the bulk of its life, then plays frames
 * 8-9 as it burns out.
 */
public class FireGroundParticle extends AbstractFireParticle {

    private static final int TOTAL_FRAMES = 10;
    private static final int IGNITION_TICKS = 4;
    private static final int DECAY_TICKS = 6;
    private static final int LOOP_START_FRAME = 3;
    private static final int LOOP_FRAME_COUNT = 5;
    private static final int LOOP_TICKS_PER_FRAME = 2;

    protected FireGroundParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.quadSize = 1.0F + this.random.nextFloat() * 0.8F;
        this.lifetime = 30 + this.random.nextInt(31);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.alpha = 0.0F;
    }

    @Override
    protected void updateSprite() {
        int decayStart = this.lifetime - DECAY_TICKS;
        int frame;
        if (this.age < IGNITION_TICKS) {
            frame = this.age * 3 / IGNITION_TICKS;
            this.alpha = (this.age + 1) / (float) (IGNITION_TICKS + 1);
        } else if (this.age >= decayStart) {
            int decayAge = this.age - decayStart;
            frame = 8 + Math.min(decayAge * 2 / DECAY_TICKS, 1);
            this.alpha = 1.0F - (float) decayAge / (float) DECAY_TICKS;
        } else {
            int loopAge = (this.age - IGNITION_TICKS) / LOOP_TICKS_PER_FRAME;
            frame = LOOP_START_FRAME + loopAge % LOOP_FRAME_COUNT;
            this.alpha = 1.0F;
        }

        this.setSprite(this.sprites.get(frame * 1000 + 500, TOTAL_FRAMES * 1000));
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
