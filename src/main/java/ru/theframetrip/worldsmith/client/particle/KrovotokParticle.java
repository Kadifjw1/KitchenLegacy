package ru.theframetrip.worldsmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class KrovotokParticle extends TextureSheetParticle {
    private enum Style {
        MIST,
        SPARK,
        PULSE,
        BURST,
        LIFE_DRAIN
    }

    private final SpriteSet sprites;
    private final Style style;
    private final float initialAlpha;
    private final float initialQuadSize;

    private KrovotokParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            SpriteSet sprites,
            Style style
    ) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.style = style;
        this.hasPhysics = false;

        switch (style) {
            case MIST -> {
                this.lifetime = 28 + level.random.nextInt(15);
                this.friction = 0.94F;
                this.gravity = -0.002F;
                this.quadSize = 0.18F + level.random.nextFloat() * 0.10F;
                this.rCol = 0.42F;
                this.gCol = 0.015F;
                this.bCol = 0.025F;
                this.alpha = 0.42F;
                this.xd *= 0.35D;
                this.yd = Math.abs(this.yd) * 0.25D + 0.004D;
                this.zd *= 0.35D;
            }
            case SPARK -> {
                this.lifetime = 8 + level.random.nextInt(6);
                this.friction = 0.88F;
                this.gravity = 0.015F;
                this.quadSize = 0.055F + level.random.nextFloat() * 0.035F;
                this.rCol = 0.95F;
                this.gCol = 0.06F;
                this.bCol = 0.035F;
                this.alpha = 0.95F;
            }
            case PULSE -> {
                this.lifetime = 14 + level.random.nextInt(7);
                this.friction = 0.91F;
                this.gravity = 0.0F;
                this.quadSize = 0.14F + level.random.nextFloat() * 0.06F;
                this.rCol = 0.66F;
                this.gCol = 0.02F;
                this.bCol = 0.035F;
                this.alpha = 0.78F;
                this.xd *= 0.55D;
                this.yd *= 0.55D;
                this.zd *= 0.55D;
            }
            case BURST -> {
                this.lifetime = 12 + level.random.nextInt(7);
                this.friction = 0.84F;
                this.gravity = 0.02F;
                this.quadSize = 0.11F + level.random.nextFloat() * 0.08F;
                this.rCol = 0.88F;
                this.gCol = 0.025F;
                this.bCol = 0.035F;
                this.alpha = 0.90F;
            }
            case LIFE_DRAIN -> {
                this.lifetime = 18 + level.random.nextInt(9);
                this.friction = 0.98F;
                this.gravity = 0.0F;
                this.quadSize = 0.075F + level.random.nextFloat() * 0.04F;
                this.rCol = 0.74F;
                this.gCol = 0.015F;
                this.bCol = 0.03F;
                this.alpha = 0.82F;
            }
        }

        this.initialAlpha = this.alpha;
        this.initialQuadSize = this.quadSize;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        this.setSpriteFromAge(this.sprites);
        float progress = Math.min(1.0F, (float) this.age / (float) this.lifetime);

        switch (this.style) {
            case MIST -> {
                this.alpha = this.initialAlpha * (1.0F - progress);
                this.quadSize = this.initialQuadSize * (1.0F + progress * 0.55F);
            }
            case SPARK -> {
                this.alpha = this.initialAlpha * (1.0F - progress * progress);
                this.quadSize = this.initialQuadSize * (1.0F - progress * 0.35F);
            }
            case PULSE -> {
                this.alpha = this.initialAlpha * (1.0F - progress);
                this.quadSize = this.initialQuadSize * (1.0F + progress * 0.85F);
            }
            case BURST -> {
                this.alpha = this.initialAlpha * (1.0F - progress);
                this.quadSize = this.initialQuadSize * (1.0F + progress * 0.30F);
            }
            case LIFE_DRAIN -> {
                this.alpha = this.initialAlpha * (1.0F - progress * 0.75F);
                this.quadSize = this.initialQuadSize * (1.0F - progress * 0.25F);
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static KrovotokParticle create(
            SimpleParticleType type,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            SpriteSet sprites,
            Style style
    ) {
        return new KrovotokParticle(level, x, y, z, xd, yd, zd, sprites, style);
    }

    public record MistProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public KrovotokParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(type, level, x, y, z, xd, yd, zd, sprites, Style.MIST);
        }
    }

    public record SparkProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public KrovotokParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(type, level, x, y, z, xd, yd, zd, sprites, Style.SPARK);
        }
    }

    public record PulseProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public KrovotokParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(type, level, x, y, z, xd, yd, zd, sprites, Style.PULSE);
        }
    }

    public record BurstProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public KrovotokParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(type, level, x, y, z, xd, yd, zd, sprites, Style.BURST);
        }
    }

    public record LifeDrainProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public KrovotokParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return create(type, level, x, y, z, xd, yd, zd, sprites, Style.LIFE_DRAIN);
        }
    }
}
