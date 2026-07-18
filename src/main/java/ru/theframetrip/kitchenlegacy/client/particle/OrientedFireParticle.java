package ru.theframetrip.kitchenlegacy.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A flat fire quad that lies fixed in world space instead of always facing
 * the camera. The incoming xSpeed/ySpeed/zSpeed is treated purely as a
 * facing direction (the strike/swing direction) - the particle does not
 * actually travel with it.
 */
public abstract class OrientedFireParticle extends AbstractFireParticle {

    private final Quaternionf orientation;

    protected OrientedFireParticle(ClientLevel level, double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);

        Vector3f facing = new Vector3f((float) xSpeed, (float) ySpeed, (float) zSpeed);
        if (facing.lengthSquared() < 1.0E-5F) {
            facing = new Vector3f(0.0F, 0.0F, 1.0F);
        } else {
            facing.normalize();
        }
        this.orientation = new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), facing);

        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
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
}
