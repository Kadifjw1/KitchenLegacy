package ru.theframetrip.worldsmith.ability.prah;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the visible Prah echo entirely from ash particles.
 *
 * <p>The ArmorStand used by the ability remains an invisible technical entity;
 * no equipment or entity outline is rendered. All visible body parts and the
 * sword are reconstructed from deterministic particle points.</p>
 */
public final class PrahEchoParticleRenderer {
    public static final int CRUMBLE_DURATION_TICKS = 10;

    private static final int SWING_DURATION_TICKS = 8;
    private static final double PARTICLE_JITTER = 0.018D;

    private PrahEchoParticleRenderer() {
    }

    public static void render(
            ServerLevel level,
            ArmorStand echo,
            PrahEchoFrame frame,
            @Nullable PrahEchoFrame previousFrame,
            int swingTicks
    ) {
        List<Vec3> points = buildWorldPoints(echo.position(), frame, swingTicks);
        for (Vec3 point : points) {
            spawnAsh(level, point, PARTICLE_JITTER);
        }

        spawnMovementTrail(level, echo.position(), frame, previousFrame);

        if (swingTicks > 0) {
            spawnSwingEmphasis(level, echo.position(), frame, swingTicks);
        }
    }

    /**
     * Renders a top-to-bottom crumble pass.
     *
     * @return true when the crumble animation has reached its final tick.
     */
    public static boolean renderCrumble(
            ServerLevel level,
            ArmorStand echo,
            PrahEchoFrame frame,
            int crumbleTick
    ) {
        List<Vec3> points = buildWorldPoints(echo.position(), frame, 0);
        double progress = Math.min(1.0D, (crumbleTick + 1.0D) / CRUMBLE_DURATION_TICKS);
        double topY = echo.getY() + 1.95D;
        double bottomY = echo.getY() - 0.02D;
        double cutoffY = topY - (topY - bottomY) * progress;

        for (Vec3 point : points) {
            if (point.y <= cutoffY) {
                spawnAsh(level, point, PARTICLE_JITTER * 1.35D);
            }
        }

        RandomSource random = level.getRandom();
        int scatterCount = crumbleTick < CRUMBLE_DURATION_TICKS - 1 ? 6 : 10;
        for (int i = 0; i < scatterCount; i++) {
            double x = echo.getX() + randomBetween(random, -0.34D, 0.34D);
            double y = cutoffY + randomBetween(random, -0.08D, 0.08D);
            double z = echo.getZ() + randomBetween(random, -0.24D, 0.24D);
            spawnAsh(level, new Vec3(x, y, z), 0.035D);
        }

        if (crumbleTick == CRUMBLE_DURATION_TICKS - 1) {
            level.sendParticles(
                    ParticleTypes.ASH,
                    echo.getX(),
                    echo.getY() + 0.08D,
                    echo.getZ(),
                    18,
                    0.35D,
                    0.08D,
                    0.35D,
                    0.025D
            );
            level.sendParticles(
                    ParticleTypes.SMOKE,
                    echo.getX(),
                    echo.getY() + 0.12D,
                    echo.getZ(),
                    4,
                    0.18D,
                    0.06D,
                    0.18D,
                    0.01D
            );
        }

        return crumbleTick >= CRUMBLE_DURATION_TICKS - 1;
    }

    public static void spawnImmediateDissolve(ServerLevel level, ArmorStand echo) {
        level.sendParticles(
                ParticleTypes.ASH,
                echo.getX(),
                echo.getY() + 0.9D,
                echo.getZ(),
                42,
                0.42D,
                0.82D,
                0.42D,
                0.045D
        );
        level.sendParticles(
                ParticleTypes.SMOKE,
                echo.getX(),
                echo.getY() + 0.55D,
                echo.getZ(),
                8,
                0.25D,
                0.42D,
                0.25D,
                0.015D
        );
    }

    private static List<Vec3> buildWorldPoints(Vec3 base, PrahEchoFrame frame, int swingTicks) {
        List<Vec3> localPoints = new ArrayList<>(55);

        double crouch = frame.crouching() ? 1.0D : 0.0D;
        double airborne = frame.onGround() ? 0.0D : 1.0D;
        double sprint = frame.sprinting() ? 1.0D : 0.0D;
        double torsoForward = 0.10D * crouch + 0.05D * sprint;

        Vec3 pelvis = new Vec3(0.0D, 0.84D - 0.12D * crouch, 0.03D * crouch);
        Vec3 chest = new Vec3(0.0D, 1.23D - 0.17D * crouch, torsoForward);
        Vec3 neck = new Vec3(0.0D, 1.49D - 0.20D * crouch, torsoForward + 0.02D);

        Vec3 leftShoulder = new Vec3(-0.28D, 1.40D - 0.18D * crouch, torsoForward);
        Vec3 rightShoulder = new Vec3(0.28D, 1.40D - 0.18D * crouch, torsoForward);

        Vec3 leftElbow = new Vec3(-0.39D, 1.13D - 0.16D * crouch, torsoForward - 0.02D);
        Vec3 leftHand = new Vec3(-0.31D, 0.88D - 0.14D * crouch, torsoForward - 0.04D);

        double swingProgress = swingTicks > 0
                ? 1.0D - swingTicks / (double) SWING_DURATION_TICKS
                : 0.0D;
        double swingArc = swingTicks > 0 ? Math.sin(swingProgress * Math.PI) : 0.0D;

        Vec3 rightHand = new Vec3(
                0.32D - 0.22D * swingArc,
                0.90D - 0.14D * crouch + 0.28D * swingArc,
                torsoForward - 0.06D + 0.52D * swingArc
        );
        Vec3 rightElbow = rightShoulder.lerp(rightHand, 0.52D)
                .add(0.08D * (1.0D - swingArc), -0.02D, -0.02D);

        Vec3 leftHip = new Vec3(-0.14D, 0.80D - 0.12D * crouch, 0.02D * crouch);
        Vec3 rightHip = new Vec3(0.14D, 0.80D - 0.12D * crouch, 0.02D * crouch);

        Vec3 leftKnee = new Vec3(
                -0.15D,
                0.43D - 0.08D * crouch + 0.08D * airborne,
                0.05D + 0.12D * crouch + 0.08D * airborne
        );
        Vec3 rightKnee = new Vec3(
                0.15D,
                0.43D - 0.08D * crouch + 0.08D * airborne,
                0.05D + 0.12D * crouch + 0.08D * airborne
        );
        Vec3 leftFoot = new Vec3(
                -0.15D,
                0.06D + 0.16D * airborne,
                0.09D + 0.10D * crouch + 0.10D * airborne
        );
        Vec3 rightFoot = new Vec3(
                0.15D,
                0.06D + 0.16D * airborne,
                0.09D + 0.10D * crouch + 0.10D * airborne
        );

        // Torso: a central spine and shoulder line.
        addLine(localPoints, pelvis, neck, 4);
        addLine(localPoints, leftShoulder, rightShoulder, 3);

        // Arms and legs use three points per segment so the silhouette remains readable.
        addLine(localPoints, leftShoulder, leftElbow, 3);
        addLine(localPoints, leftElbow, leftHand, 3);
        addLine(localPoints, rightShoulder, rightElbow, 3);
        addLine(localPoints, rightElbow, rightHand, 3);
        addLine(localPoints, leftHip, leftKnee, 3);
        addLine(localPoints, leftKnee, leftFoot, 3);
        addLine(localPoints, rightHip, rightKnee, 3);
        addLine(localPoints, rightKnee, rightFoot, 3);

        addHeadCorners(localPoints, new Vec3(
                0.0D,
                1.70D - 0.20D * crouch,
                torsoForward + 0.03D + pitchOffset(frame.xRot())
        ));

        Vec3 restSwordDirection = new Vec3(0.06D, -0.78D, -0.52D).normalize();
        Vec3 strikeSwordDirection = new Vec3(-0.18D, 0.05D, 0.98D).normalize();
        Vec3 swordDirection = restSwordDirection.lerp(strikeSwordDirection, swingArc).normalize();

        Vec3 handleEnd = rightHand.subtract(swordDirection.scale(0.18D));
        Vec3 guardCenter = rightHand.add(swordDirection.scale(0.08D));
        Vec3 bladeStart = rightHand.add(swordDirection.scale(0.13D));
        Vec3 bladeEnd = rightHand.add(swordDirection.scale(1.05D));

        addLine(localPoints, handleEnd, rightHand, 2);
        addLine(
                localPoints,
                guardCenter.add(-0.16D, 0.0D, 0.0D),
                guardCenter.add(0.16D, 0.0D, 0.0D),
                3
        );
        addLine(localPoints, bladeStart, bladeEnd, 5);

        Basis basis = Basis.from(base, frame.yRot());
        List<Vec3> worldPoints = new ArrayList<>(localPoints.size());
        for (Vec3 local : localPoints) {
            worldPoints.add(basis.toWorld(local));
        }
        return worldPoints;
    }

    private static void addHeadCorners(List<Vec3> points, Vec3 center) {
        double halfX = 0.21D;
        double halfY = 0.21D;
        double halfZ = 0.19D;
        for (int xSign : new int[]{-1, 1}) {
            for (int ySign : new int[]{-1, 1}) {
                for (int zSign : new int[]{-1, 1}) {
                    points.add(center.add(
                            halfX * xSign,
                            halfY * ySign,
                            halfZ * zSign
                    ));
                }
            }
        }
    }

    private static void addLine(List<Vec3> points, Vec3 start, Vec3 end, int pointCount) {
        if (pointCount <= 1) {
            points.add(start);
            return;
        }
        for (int i = 0; i < pointCount; i++) {
            points.add(start.lerp(end, i / (double) (pointCount - 1)));
        }
    }

    private static void spawnMovementTrail(
            ServerLevel level,
            Vec3 currentBase,
            PrahEchoFrame frame,
            @Nullable PrahEchoFrame previousFrame
    ) {
        if (previousFrame == null) {
            return;
        }

        Vec3 movement = new Vec3(
                frame.x() - previousFrame.x(),
                frame.y() - previousFrame.y(),
                frame.z() - previousFrame.z()
        );
        if (movement.lengthSqr() < 0.0004D) {
            return;
        }

        Vec3 behind = movement.normalize().scale(-1.0D);
        double[] heights = {0.16D, 0.88D, 1.32D};
        for (int i = 0; i < heights.length; i++) {
            Vec3 trailPoint = currentBase
                    .add(0.0D, heights[i], 0.0D)
                    .add(behind.scale(0.16D + i * 0.09D));
            spawnAsh(level, trailPoint, 0.035D);
        }
    }

    private static void spawnSwingEmphasis(
            ServerLevel level,
            Vec3 base,
            PrahEchoFrame frame,
            int swingTicks
    ) {
        double progress = 1.0D - swingTicks / (double) SWING_DURATION_TICKS;
        double arc = Math.sin(progress * Math.PI);
        Basis basis = Basis.from(base, frame.yRot());

        Vec3 hand = new Vec3(
                0.32D - 0.22D * arc,
                0.90D - (frame.crouching() ? 0.14D : 0.0D) + 0.28D * arc,
                (frame.crouching() ? 0.10D : 0.0D)
                        + (frame.sprinting() ? 0.05D : 0.0D)
                        - 0.06D
                        + 0.52D * arc
        );
        Vec3 restDirection = new Vec3(0.06D, -0.78D, -0.52D).normalize();
        Vec3 strikeDirection = new Vec3(-0.18D, 0.05D, 0.98D).normalize();
        Vec3 direction = restDirection.lerp(strikeDirection, arc).normalize();

        for (double distance : new double[]{0.35D, 0.68D, 0.96D}) {
            Vec3 localPoint = hand.add(direction.scale(distance)).add(0.025D, 0.0D, 0.0D);
            spawnAsh(level, basis.toWorld(localPoint), 0.028D);
        }
    }

    private static double pitchOffset(float xRot) {
        double clamped = Math.max(-60.0D, Math.min(60.0D, xRot));
        return Math.sin(Math.toRadians(clamped)) * 0.08D;
    }

    private static void spawnAsh(ServerLevel level, Vec3 point, double jitter) {
        RandomSource random = level.getRandom();
        level.sendParticles(
                ParticleTypes.ASH,
                point.x + randomBetween(random, -jitter, jitter),
                point.y + randomBetween(random, -jitter, jitter),
                point.z + randomBetween(random, -jitter, jitter),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }

    private static double randomBetween(RandomSource random, double minimum, double maximum) {
        return minimum + random.nextDouble() * (maximum - minimum);
    }

    private record Basis(Vec3 origin, Vec3 right, Vec3 up, Vec3 forward) {
        private static Basis from(Vec3 origin, float yRot) {
            Vec3 forward = Vec3.directionFromRotation(0.0F, yRot).normalize();
            Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
            return new Basis(origin, right, new Vec3(0.0D, 1.0D, 0.0D), forward);
        }

        private Vec3 toWorld(Vec3 local) {
            return origin
                    .add(right.scale(local.x))
                    .add(up.scale(local.y))
                    .add(forward.scale(local.z));
        }
    }
}
