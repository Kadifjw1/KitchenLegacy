package ru.theframetrip.kitchenlegacy.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Flame shockwave that travels forward along the ground, lying flat and
 * facing the direction of travel instead of the camera. The incoming
 * xSpeed/zSpeed is used purely as a direction; actual travel speed is
 * fixed to the 0.4-0.8 blocks/tick range from spec.
 */
public class FireWaveParticle extends AbstractFireParticle {

    private static final float DECAY_START_FRACTION = 0.5F;

    private final Quaternionf orientation;

    protected FireWaveParticle(ClientLevel level, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.friction = 1.0F;
        this.quadSize = 2.5F + this.random.nextFloat() * 2.0F;
        this.lifetime = 10 + this.random.nextInt(7);

        double dirX = xSpeed;
        double dirZ = zSpeed;
        double horizontalLength = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (horizontalLength < 1.0E-5) {
            dirX = 0.0;
            dirZ = 1.0;
            horizontalLength = 1.0;
        }
        dirX /= horizontalLength;
        dirZ /= horizontalLength;

        float speed = 0.4F + this.random.nextFloat() * 0.4F;
        this.xd = dirX * speed;
        this.yd = 0.0;
        this.zd = dirZ * speed;

        Quaternionf flatten = new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(0.0F, 1.0F, 0.0F));
        float yaw = (float) Mth.atan2(dirX, dirZ);
        this.orientation = new Quaternionf().rotationY(yaw).mul(flatten);
    }

    @Override
    protected void updateSprite() {
        this.setSpriteFromAge(this.sprites);
        this.quadSize *= 1.05F;
        float lifeFraction = (float) this.age / (float) this.lifetime;
        if (lifeFraction > DECAY_START_FRACTION) {
            this.alpha = 1.0F - (lifeFraction - DECAY_START_FRACTION) / (1.0F - DECAY_START_FRACTION);
        } else {
            this.alpha = 1.0F;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 camPos = renderInfo.getPosition();
        float x = (float) (Mth.lerp((double) partialTicks, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp((double) partialTicks, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp((double) partialTicks, this.zo, this.z) - camPos.z());

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float size = this.getQuadSize(partialTicks);

        for (Vector3f corner : corners) {
            corner.rotate(this.orientation);
            corner.mul(size);
            corner.add(x, y, z);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);

        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            return new FireWaveParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
