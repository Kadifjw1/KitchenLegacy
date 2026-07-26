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

        // Approved PNGs already contain the final palette. Never tint them.
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        switch (style) {
            case FLAME -> {
                this.lifetime = 7 + level.random.nextInt(4);
                this.friction = 0.91F;
                this.gravity = -0.014F;
                this.quadSize = 0.045F + level.random.nextFloat() * 0.022F;
                this.alpha = 0.96F;
                this.xd *= 0.22D;
                this.yd = 0.008D + level.random.nextDouble() * 0.015D;
                this.zd *= 0.22D;
                this.setSpriteFromAge(sprites);
            }
            case EMBER -> {
                this.lifetime = 12 + level.random.nextInt(8);
                this.friction = 0.95F;
                this.gravity = -0.009F;
                this.quadSize = 0.024F + level.random.nextFloat() * 0.012F;
                this.alpha = 0.96F;
                this.xd *= 0.42D;
                this.yd = 0.006D + level.random.nextDouble() * 0.016D;
                this.zd *= 0.42D;
                this.pickSprite(sprites);
            }
            case SPARK -> {
                this.lifetime = 4 + level.random.nextInt(4);
                this.friction = 0.83F;
                this.gravity = 0.11F;
                this.quadSize = 0.020F + level.random.nextFloat() * 0.010F;
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
            this.xd += (this.random.nextDouble() - 0.5D) * 0.0010D;
            this.zd += (this.random.nextDouble() - 0.5D) * 0.0010D;
        }

        float progress = Math.min(1.0F, (float) this.age / (float) this.lifetime);
        this.alpha = this.initialAlpha * Mth.clamp(1.0F - progress, 0.0F, 1.0F);

        switch (this.style) {
            case FLAME -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.24F);
            case EMBER -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.32F);
            case SPARK -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.68F);
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
