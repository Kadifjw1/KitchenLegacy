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

        // Keep the PNG palette intact. The old warm tint washed the sprites into
        // oversized peach rectangles when several particles overlapped.
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        switch (style) {
            case FLAME -> {
                this.lifetime = 5 + level.random.nextInt(4);
                this.friction = 0.91F;
                this.gravity = -0.015F;
                this.quadSize = 0.035F + level.random.nextFloat() * 0.020F;
                this.alpha = 0.88F;
                this.xd *= 0.25D;
                this.yd = 0.006D + level.random.nextDouble() * 0.012D;
                this.zd *= 0.25D;
                this.setSpriteFromAge(sprites);
            }
            case EMBER -> {
                this.lifetime = 10 + level.random.nextInt(7);
                this.friction = 0.95F;
                this.gravity = -0.008F;
                this.quadSize = 0.018F + level.random.nextFloat() * 0.010F;
                this.alpha = 0.92F;
                this.xd *= 0.45D;
                this.yd = 0.005D + level.random.nextDouble() * 0.014D;
                this.zd *= 0.45D;
                this.pickSprite(sprites);
            }
            case SPARK -> {
                this.lifetime = 4 + level.random.nextInt(4);
                this.friction = 0.84F;
                this.gravity = 0.10F;
                this.quadSize = 0.014F + level.random.nextFloat() * 0.009F;
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
            this.xd += (this.random.nextDouble() - 0.5D) * 0.0012D;
            this.zd += (this.random.nextDouble() - 0.5D) * 0.0012D;
        }

        float progress = Math.min(1.0F, (float) this.age / (float) this.lifetime);
        this.alpha = this.initialAlpha * Mth.clamp(1.0F - progress, 0.0F, 1.0F);

        switch (this.style) {
            case FLAME -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.28F);
            case EMBER -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.35F);
            case SPARK -> this.quadSize = this.initialQuadSize * (1.0F - progress * 0.65F);
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
