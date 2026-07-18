package ru.theframetrip.kitchenlegacy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Rising flame attached to the blade/hand while a charged attack is held.
 * Intensity and size ramp up the longer it lives, and the animation loops.
 * <p>
 * The lifetime here is only a safety cap (this class has no way to know how
 * long the attack is actually held) - the calling code that spawns and
 * tracks this particle each tick while charging is expected to reposition
 * it to follow the hand and remove it once the attack fires or is released.
 */
public class FireChargeParticle extends AbstractFireParticle {

    private static final int SAFETY_LIFETIME = 200;
    private static final int LOOP_LENGTH = 10;
    private static final float RAMP_UP_TICKS = 40.0F;

    protected FireChargeParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.quadSize = 0.8F;
        this.lifetime = SAFETY_LIFETIME;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.alpha = 0.2F;
    }

    @Override
    protected void updateSprite() {
        int loopAge = this.age % LOOP_LENGTH;
        this.setSprite(this.sprites.get(loopAge, LOOP_LENGTH));

        float intensity = Math.min(this.age / RAMP_UP_TICKS, 1.0F);
        this.alpha = 0.2F + 0.8F * intensity;
        this.quadSize = 0.8F + 0.6F * intensity;
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
