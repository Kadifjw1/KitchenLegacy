package ru.theframetrip.worldsmith.ability.prah;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the visible Prah echo entirely from ash particles.
 *
 * <p>The ArmorStand used by the ability remains an invisible technical entity.
 * Body proportions are based on the Minecraft player silhouette, while the
 * sword particle cloud is generated from the exact uploaded item model.</p>
 */
public final class PrahEchoParticleRenderer {
    public static final int CRUMBLE_DURATION_TICKS = 10;

    private static final int SWING_DURATION_TICKS = 8;
    private static final double PARTICLE_JITTER = 0.014D;

    private PrahEchoParticleRenderer() {
    }

    public static void render(
            ServerLevel level,
            ArmorStand echo,
            PrahEchoFrame frame,
            @Nullable PrahEchoFrame previousFrame,
            int swingTicks
    ) {
        int swordPhase = Math.floorMod(echo.tickCount / 2, 3);
        boolean fullSwordDensity = swingTicks > 0;

        List<Vec3> points = buildWorldPoints(
                echo.position(),
                frame,
                swingTicks,
                swordPhase,
                fullSwordDensity
        );

        for (Vec3 point : points) {
            spawnAsh(level, point, PARTICLE_JITTER);
        }

        spawnMovementTrail(level, echo.position(), frame, previousFrame);

        if (swingTicks > 0) {
            spawnSwordTipTrail(level, echo.position(), frame, swingTicks);
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
        List<Vec3> points = buildWorldPoints(
                echo.position(),
                frame,
                0,
                crumbleTick,
                false
        );

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
        int scatterCount = crumbleTick < CRUMBLE_DURATION_TICKS - 1 ? 12 : 20;
        for (int index = 0; index < scatterCount; index++) {
            double x = echo.getX() + randomBetween(random, -0.38D, 0.38D);
            double y = cutoffY + randomBetween(random, -0.11D, 0.11D);
            double z = echo.getZ() + randomBetween(random, -0.28D, 0.28D);
            spawnAsh(level, new Vec3(x, y, z), 0.040D);
        }

        if (crumbleTick == CRUMBLE_DURATION_TICKS - 1) {
            level.sendParticles(
                    ModParticleTypes.PRAH_ASH.get(),
                    echo.getX(),
                    echo.getY() + 0.08D,
                    echo.getZ(),
                    32,
                    0.40D,
                    0.11D,
                    0.40D,
                    0.02D
            );
            level.sendParticles(
                    ParticleTypes.SMOKE,
                    echo.getX(),
                    echo.getY() + 0.12D,
                    echo.getZ(),
                    6,
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
                ModParticleTypes.PRAH_ASH.get(),
                echo.getX(),
                echo.getY() + 0.9D,
                echo.getZ(),
                64,
                0.46D,
                0.90D,
                0.46D,
                0.035D
        );
        level.sendParticles(
                ParticleTypes.SMOKE,
                echo.getX(),
                echo.getY() + 0.55D,
                echo.getZ(),
                10,
                0.25D,
                0.42D,
                0.25D,
                0.015D
        );
    }

    private static List<Vec3> buildWorldPoints(
            Vec3 base,
            PrahEchoFrame frame,
            int swingTicks,
            int swordPhase,
            boolean fullSwordDensity
    ) {
        List<Vec3> localPoints = new ArrayList<>(180);

        double crouch = frame.crouching() ? 1.0D : 0.0D;
        double airborne = frame.onGround() ? 0.0D : 1.0D;
        double sprint = frame.sprinting() ? 1.0D : 0.0D;
        double torsoForward = 0.10D * crouch + 0.05D * sprint;

        Vec3 pelvis = new Vec3(0.0D, 0.82D - 0.12D * crouch, 0.03D * crouch);
        Vec3 chest = new Vec3(0.0D, 1.20D - 0.17D * crouch, torsoForward);
        Vec3 neck = new Vec3(0.0D, 1.48D - 0.20D * crouch, torsoForward + 0.02D);

        Vec3 leftShoulder = new Vec3(-0.29D, 1.39D - 0.18D * crouch, torsoForward);
        Vec3 rightShoulder = new Vec3(0.29D, 1.39D - 0.18D * crouch, torsoForward);

        Vec3 leftElbow = new Vec3(-0.40D, 1.12D - 0.16D * crouch, torsoForward - 0.02D);
        Vec3 leftHand = new Vec3(-0.33D, 0.86D - 0.14D * crouch, torsoForward - 0.04D);

        double swingProgress = swingTicks > 0
                ? 1.0D - swingTicks / (double) SWING_DURATION_TICKS
                : 0.0D;
        double swingArc = swingTicks > 0 ? Math.sin(swingProgress * Math.PI) : 0.0D;

        Vec3 rightHand = new Vec3(
                0.33D - 0.22D * swingArc,
                0.87D - 0.14D * crouch + 0.28D * swingArc,
                torsoForward - 0.06D + 0.52D * swingArc
        );
        Vec3 rightElbow = rightShoulder.lerp(rightHand, 0.52D)
                .add(0.08D * (1.0D - swingArc), -0.02D, -0.02D);

        Vec3 leftHip = new Vec3(-0.14D, 0.79D - 0.12D * crouch, 0.02D * crouch);
        Vec3 rightHip = new Vec3(0.14D, 0.79D - 0.12D * crouch, 0.02D * crouch);

        Vec3 leftKnee = new Vec3(
                -0.15D,
                0.42D - 0.08D * crouch + 0.08D * airborne,
                0.05D + 0.12D * crouch + 0.08D * airborne
        );
        Vec3 rightKnee = new Vec3(
                0.15D,
                0.42D - 0.08D * crouch + 0.08D * airborne,
                0.05D + 0.12D * crouch + 0.08D * airborne
        );
        Vec3 leftFoot = new Vec3(
                -0.15D,
                0.05D + 0.16D * airborne,
                0.09D + 0.10D * crouch + 0.10D * airborne
        );
        Vec3 rightFoot = new Vec3(
                0.15D,
                0.05D + 0.16D * airborne,
                0.09D + 0.10D * crouch + 0.10D * airborne
        );

        addLine(localPoints, pelvis, neck, 7);
        addLine(localPoints, leftShoulder, rightShoulder, 6);
        addLine(localPoints, chest.add(-0.20D, 0.08D, 0.0D), chest.add(0.20D, 0.08D, 0.0D), 5);
        addLine(localPoints, chest.add(-0.17D, -0.08D, 0.0D), chest.add(0.17D, -0.08D, 0.0D), 5);
        addLine(localPoints, chest.add(-0.14D, -0.20D, 0.0D), chest.add(0.14D, -0.20D, 0.0D), 4);
        addLine(localPoints, leftShoulder, pelvis.add(-0.09D, 0.0D, 0.01D), 5);
        addLine(localPoints, rightShoulder, pelvis.add(0.09D, 0.0D, 0.01D), 5);
        localPoints.add(chest);
        localPoints.add(chest.add(0.0D, -0.11D, 0.01D));
        localPoints.add(pelvis);

        addLine(localPoints, leftShoulder, leftElbow, 5);
        addLine(localPoints, leftElbow, leftHand, 5);
        addLine(localPoints, rightShoulder, rightElbow, 5);
        addLine(localPoints, rightElbow, rightHand, 5);

        addLine(localPoints, leftHip, leftKnee, 5);
        addLine(localPoints, leftKnee, leftFoot, 5);
        addLine(localPoints, rightHip, rightKnee, 5);
        addLine(localPoints, rightKnee, rightFoot, 5);

        addHeadSilhouette(localPoints, new Vec3(
                0.0D,
                1.69D - 0.20D * crouch,
                torsoForward + 0.03D + pitchOffset(frame.xRot())
        ));

        Vec3 swordDirection = calculateSwordDirection(swingArc);
        PrahSwordParticleShape.addPoints(
                localPoints,
                rightHand,
                swordDirection,
                swordPhase,
                fullSwordDensity
        );

        Basis basis = Basis.from(base, frame.yRot());
        List<Vec3> worldPoints = new ArrayList<>(localPoints.size());
        for (Vec3 local : localPoints) {
            worldPoints.add(basis.toWorld(local));
        }
        return worldPoints;
    }

    private static Vec3 calculateSwordDirection(double swingArc) {
        Vec3 restDirection = new Vec3(0.06D, -0.78D, -0.52D).normalize();
        Vec3 strikeDirection = new Vec3(-0.18D, 0.05D, 0.98D).normalize();
        return restDirection.lerp(strikeDirection, swingArc).normalize();
    }

    private static void addHeadSilhouette(List<Vec3> points, Vec3 center) {
        double halfX = 0.21D;
        double halfY = 0.21D;
        double halfZ = 0.19D;

        Vec3[] corners = new Vec3[8];
        int index = 0;
        for (int xSign : new int[]{-1, 1}) {
            for (int ySign : new int[]{-1, 1}) {
                for (int zSign : new int[]{-1, 1}) {
                    Vec3 corner = center.add(
                            halfX * xSign,
                            halfY * ySign,
                            halfZ * zSign
                    );
                    corners[index++] = corner;
                    points.add(corner);
                }
            }
        }

        int[][] edgePairs = {
                {0, 1}, {0, 2}, {0, 4},
                {1, 3}, {1, 5},
                {2, 3}, {2, 6},
                {3, 7},
                {4, 5}, {4, 6},
                {5, 7}, {6, 7}
        };
        for (int[] edgePair : edgePairs) {
            points.add(corners[edgePair[0]].lerp(corners[edgePair[1]], 0.5D));
        }

        points.add(center);
        points.add(center.add(0.0D, 0.10D, 0.0D));
        points.add(center.add(0.0D, -0.10D, 0.0D));
        points.add(center.add(0.0D, 0.0D, halfZ * 0.55D));
        points.add(center.add(0.0D, 0.0D, -halfZ * 0.55D));
        points.add(center.add(-halfX * 0.45D, 0.0D, 0.0D));
        points.add(center.add(halfX * 0.45D, 0.0D, 0.0D));
    }

    private static void addLine(List<Vec3> points, Vec3 start, Vec3 end, int pointCount) {
        if (pointCount <= 1) {
            points.add(start);
            return;
        }

        for (int index = 0; index < pointCount; index++) {
            points.add(start.lerp(end, index / (double) (pointCount - 1)));
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
        double[] heights = {0.10D, 0.30D, 0.56D, 0.86D, 1.14D, 1.40D};
        for (int index = 0; index < heights.length; index++) {
            Vec3 trailPoint = currentBase
                    .add(0.0D, heights[index], 0.0D)
                    .add(behind.scale(0.14D + index * 0.065D));
            spawnAsh(level, trailPoint, 0.035D);
        }
    }

    private static void spawnSwordTipTrail(
            ServerLevel level,
            Vec3 base,
            PrahEchoFrame frame,
            int swingTicks
    ) {
        double progress = 1.0D - swingTicks / (double) SWING_DURATION_TICKS;
        double arc = Math.sin(progress * Math.PI);
        double crouch = frame.crouching() ? 1.0D : 0.0D;
        double sprint = frame.sprinting() ? 1.0D : 0.0D;
        double torsoForward = 0.10D * crouch + 0.05D * sprint;

        Vec3 hand = new Vec3(
                0.33D - 0.22D * arc,
                0.87D - 0.14D * crouch + 0.28D * arc,
                torsoForward - 0.06D + 0.52D * arc
        );
        Vec3 direction = calculateSwordDirection(arc);
        Vec3 localTip = PrahSwordParticleShape.tipPoint(hand, direction);
        Basis basis = Basis.from(base, frame.yRot());

        for (int index = 0; index < 7; index++) {
            double offset = index * 0.035D;
            Vec3 localTrailPoint = localTip.subtract(direction.scale(offset));
            spawnAsh(
                    level,
                    basis.toWorld(localTrailPoint),
                    0.030D
            );
        }
    }

    private static double pitchOffset(float xRot) {
        double clamped = Math.max(-60.0D, Math.min(60.0D, xRot));
        return Math.sin(Math.toRadians(clamped)) * 0.08D;
    }

    private static void spawnAsh(ServerLevel level, Vec3 point, double jitter) {
        RandomSource random = level.getRandom();
        level.sendParticles(
                ModParticleTypes.PRAH_ASH.get(),
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
