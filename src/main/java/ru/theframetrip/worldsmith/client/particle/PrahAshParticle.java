package ru.theframetrip.worldsmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Soft custom ash particle used to build the visible Prah echo silhouette.
 * Each instance randomly selects one of the nine uploaded textures and keeps it
 * for its full lifetime, so the silhouette looks irregular instead of animated
 * through every sprite in sequence.
 */
public final class PrahAshParticle extends TextureSheetParticle {
    private final float initialAlpha;
    private final float initialQuadSize;
    private final float rollVelocity;
    private final float driftPhase;

    private PrahAshParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xd, yd, zd);

        RandomSource random = level.random;
        this.hasPhysics = false;
        this.friction = 0.96F;
        this.gravity = 0.0015F;
        this.lifetime = 10 + random.nextInt(6);
        this.quadSize = 0.105F + random.nextFloat() * 0.075F;

        float shade = 0.70F + random.nextFloat() * 0.18F;
        this.rCol = shade;
        this.gCol = shade * 0.98F;
        this.bCol = shade * 0.95F;
        this.alpha = 0.58F + random.nextFloat() * 0.22F;

        this.initialAlpha = this.alpha;
        this.initialQuadSize = this.quadSize;
        this.roll = random.nextFloat() * ((float) Math.PI * 2.0F);
        this.oRoll = this.roll;
        this.rollVelocity = (random.nextFloat() - 0.5F) * 0.055F;
        this.driftPhase = random.nextFloat() * ((float) Math.PI * 2.0F);

        this.xd = xd * 0.18D + (random.nextDouble() - 0.5D) * 0.004D;
        this.yd = yd * 0.18D + 0.0015D + random.nextDouble() * 0.003D;
        this.zd = zd * 0.18D + (random.nextDouble() - 0.5D) * 0.004D;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        if (this.removed) {
            return;
        }

        this.roll += this.rollVelocity;

        float progress = Math.min(1.0F, (float) this.age / (float) this.lifetime);
        float fadeIn = Mth.clamp(this.age / 2.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((this.lifetime - this.age) / 4.0F, 0.0F, 1.0F);
        this.alpha = this.initialAlpha * fadeIn * fadeOut;
        this.quadSize = this.initialQuadSize * (1.0F + progress * 0.20F);

        double wobble = Math.sin(this.age * 0.52D + this.driftPhase) * 0.00035D;
        this.xd += wobble;
        this.zd -= wobble * 0.8D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public PrahAshParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            return new PrahAshParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
