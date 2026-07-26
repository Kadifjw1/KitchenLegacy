package ru.theframetrip.worldsmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class NakalParticle extends TextureSheetParticle {
    private enum Style {
        FLAME,
        EMBER,
        SPARK
    }

    private final SpriteSet sprites;
    private final Style style;
    private final float initialAlpha;
    private final float initialQuadSize;

    private NakalParticle(
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
            case FLAME -> {
                this.lifetime = 8 + level.random.nextInt(7);
                this.friction = 0.88F;
                this.gravity = -0.035F;
                this.quadSize = 0.065F + level.random.nextFloat() * 0.04F;
                this.rCol = 1.0F;
                this.gCol = 0.84F;
                this.bCol = 0.70F;
                this.alpha = 0.95F;
                this.xd *= 0.35D;
                this.yd = 0.012D + level.random.nextDouble() * 0.020D;
                this.zd *= 0.35D;
                this.setSpriteFromAge(sprites);
            }
            case EMBER -> {
                this.lifetime = 14 + level.random.nextInt(10);
                this.friction = 0.94F;
                this.gravity = -0.012F;
                this.quadSize = 0.035F + level.random.nextFloat() * 0.028F;
                this.rCol = 1.0F;
                this.gCol = 0.74F;
                this.bCol = 0.48F;
                this.alpha = 0.92F;
                this.xd *= 0.55D;
                this.yd = 0.008D + level.random.nextDouble() * 0.020D;
                this.zd *= 0.55D;
                this.pickSprite(sprites);
            }
            case SPARK -> {
                this.lifetime = 4 + level.random.nextInt(5);
                this.friction = 0.82F;
                this.gravity = 0.18F;
                this.quadSize = 0.025F + level.random.nextFloat() * 0.028F;
                this.rCol = 1.0F;
                this.gCol = 0.92F;
                this.bCol = 0.72F;
                this.alpha = 1.0F;
                this.pickSprite(sprites);
            }
        }

        this.initialAlpha = this.alpha;
        this.initialQuadSize = this.quadSize;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        if (this.style == Style.FLAME) {
            this.setSpriteFromAge(this.sprites);
            this.xd += (this.random.nextDouble() - 0.5D) * 0.0025D;
            this.zd += (this.random.nextDouble() - 0.5D) * 0.0025D;
        }

        float progress = Math.min(1.0F, (float) this.age / (float) this.lifetime);
        this.alpha = this.initialAlpha * Mth.clamp(1.0F - progress, 0.0F, 1.0F);

        switch (this.style) {
            case FLAME -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.15F);
            case EMBER -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.25F);
            case SPARK -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.55F);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static NakalParticle create(
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
        return new NakalParticle(level, x, y, z, xd, yd, zd, sprites, style);
    }

    public record FlameProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public NakalParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            return create(level, x, y, z, xd, yd, zd, sprites, Style.FLAME);
        }
    }

    public record EmberProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public NakalParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            return create(level, x, y, z, xd, yd, zd, sprites, Style.EMBER);
        }
    }

    public record SparkProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public NakalParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            return create(level, x, y, z, xd, yd, zd, sprites, Style.SPARK);
        }
    }
}
