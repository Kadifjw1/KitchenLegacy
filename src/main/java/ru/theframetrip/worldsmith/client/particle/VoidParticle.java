package ru.theframetrip.worldsmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class VoidParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected VoidParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.lifetime = 18 + level.random.nextInt(12);
        this.gravity = 0.0F;
        this.friction = 0.86F;
        this.quadSize = 0.12F + level.random.nextFloat() * 0.08F;
        this.rCol = 0.35F;
        this.gCol = 0.08F;
        this.bCol = 0.55F;
        this.alpha = 0.75F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = Math.max(0.0F, 0.75F * (1.0F - (float) this.age / (float) this.lifetime));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public VoidParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new VoidParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
