package ru.theframetrip.worldsmith.ability.prah;

import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Exact particle projection of the uploaded sword JSON.
 *
 * <p>128 representative element centres are anchored at the centre of the
 * handle grip (8, -3.5, 8) and converted with the model's third-person scale
 * of 0.45. The points preserve the blade taper, guard, handle and pommel.</p>
 */
public final class PrahSwordParticleShape {
    private static final int PHASE_COUNT = 3;
    private static final double TIP_DISTANCE = 0.82968750D;

    /**
     * Alternating X offset from the grip and Y distance along the sword axis.
     */
    private static final double[] POINTS = {
            0.00000000D, 0.16875000D, -0.05625000D, 0.18281250D, 0.00000000D, 0.18281250D, 0.05625000D, 0.18281250D,
            -0.07031250D, 0.18351563D, 0.07031250D, 0.18351563D, -0.05625000D, 0.21093750D, 0.00000000D, 0.21093750D,
            0.05625000D, 0.21093750D, -0.05625000D, 0.23906250D, 0.00000000D, 0.23906250D, 0.05625000D, 0.23906250D,
            -0.05625000D, 0.26718750D, 0.00000000D, 0.26718750D, 0.05625000D, 0.26718750D, -0.08437500D, 0.29531250D,
            0.00000000D, 0.29531250D, 0.05625000D, 0.29531250D, -0.08437500D, 0.32343750D, 0.00000000D, 0.32343750D,
            0.05625000D, 0.32343750D, -0.05625000D, 0.35156250D, 0.00000000D, 0.35156250D, 0.05625000D, 0.35156250D,
            -0.08437500D, 0.37968750D, 0.00000000D, 0.37968750D, 0.05625000D, 0.37968750D, -0.08437500D, 0.40781250D,
            0.00000000D, 0.40781250D, 0.05625000D, 0.40781250D, -0.05625000D, 0.43593750D, 0.00000000D, 0.43593750D,
            0.05625000D, 0.43593750D, -0.08437500D, 0.46406250D, 0.00000000D, 0.46406250D, 0.05625000D, 0.46406250D,
            -0.08437500D, 0.49218750D, 0.00000000D, 0.49218750D, 0.05625000D, 0.49218750D, -0.05625000D, 0.52031250D,
            0.00000000D, 0.52031250D, 0.05625000D, 0.52031250D, -0.08437500D, 0.54843750D, 0.00000000D, 0.54843750D,
            0.05625000D, 0.54843750D, -0.08437500D, 0.57656250D, 0.00000000D, 0.57656250D, 0.05625000D, 0.57656250D,
            -0.05625000D, 0.60468750D, 0.00000000D, 0.60468750D, 0.05625000D, 0.60468750D, -0.08437500D, 0.63281250D,
            0.00000000D, 0.63281250D, 0.05625000D, 0.63281250D, -0.08437500D, 0.66093750D, 0.00000000D, 0.66093750D,
            0.05625000D, 0.66093750D, -0.05625000D, 0.68906250D, 0.00000000D, 0.68906250D, 0.05625000D, 0.68906250D,
            -0.05625000D, 0.71718750D, 0.00000000D, 0.71718750D, 0.05625000D, 0.71718750D, -0.02812500D, 0.74531250D,
            0.00000000D, 0.74531250D, 0.02812500D, 0.74531250D, -0.02812500D, 0.77343750D, 0.00000000D, 0.77343750D,
            0.02812500D, 0.77343750D, 0.00000000D, 0.80156250D, 0.00000000D, 0.82968750D, -0.14062500D, 0.12656250D,
            -0.11250000D, 0.12656250D, -0.08437500D, 0.12656250D, -0.05625000D, 0.12656250D, -0.02812500D, 0.12656250D,
            0.00000000D, 0.12656250D, 0.02812500D, 0.12656250D, 0.05625000D, 0.12656250D, 0.08437500D, 0.12656250D,
            0.11250000D, 0.12656250D, 0.14062500D, 0.12656250D, -0.16875000D, 0.11250000D, -0.19687500D, 0.11250000D,
            -0.19687500D, 0.14062500D, -0.16875000D, 0.16875000D, 0.16875000D, 0.11250000D, 0.19687500D, 0.11250000D,
            0.19687500D, 0.14062500D, 0.16875000D, 0.16875000D, 0.00000000D, -0.09843750D, 0.00000000D, -0.08732813D,
            0.00000000D, -0.07031250D, 0.00000000D, -0.05920313D, 0.00000000D, -0.04218750D, 0.00000000D, -0.03107813D,
            0.00000000D, -0.01406250D, 0.00000000D, -0.00295312D, 0.00000000D, 0.01406250D, 0.00000000D, 0.02517187D,
            0.00000000D, 0.04218750D, 0.00000000D, 0.05329688D, 0.00000000D, 0.07031250D, 0.00000000D, 0.08142188D,
            0.00000000D, 0.09843750D, 0.00000000D, 0.10954688D, 0.00000000D, -0.12656250D, 0.00000000D, -0.21093750D,
            -0.04218750D, -0.16875000D, 0.04218750D, -0.16875000D, 0.00000000D, -0.11390625D, -0.08718750D, 0.34171875D,
            -0.09562500D, 0.46125000D, -0.08718750D, 0.57656250D, 0.07171875D, 0.37968750D, 0.06468750D, 0.50906250D,
            0.05343750D, 0.63281250D, 0.00000000D, 0.21093750D, 0.00000000D, 0.26718750D, -0.01406250D, 0.32343750D,
            -0.01406250D, 0.37968750D, 0.00000000D, 0.43593750D, -0.01406250D, 0.49218750D, -0.01406250D, 0.54843750D,
            0.00000000D, 0.60468750D, -0.01406250D, 0.66093750D, 0.00000000D, 0.71718750D, 0.00000000D, 0.77343750D,
    };

    private PrahSwordParticleShape() {
    }

    public static void addPoints(
            List<Vec3> output,
            Vec3 hand,
            Vec3 swordDirection,
            int phase,
            boolean fullDensity
    ) {
        Vec3 axis = swordDirection.normalize();
        Vec3 preferredSide = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 side = preferredSide.subtract(axis.scale(preferredSide.dot(axis)));
        side = side.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : side.normalize();

        int normalizedPhase = Math.floorMod(phase, PHASE_COUNT);
        int pointIndex = 0;
        for (int offset = 0; offset < POINTS.length; offset += 2) {
            if (!fullDensity && pointIndex % PHASE_COUNT != normalizedPhase) {
                pointIndex++;
                continue;
            }

            output.add(
                    hand
                            .add(side.scale(POINTS[offset]))
                            .add(axis.scale(POINTS[offset + 1]))
            );
            pointIndex++;
        }
    }

    public static Vec3 tipPoint(Vec3 hand, Vec3 swordDirection) {
        return hand.add(swordDirection.normalize().scale(TIP_DISTANCE));
    }

    public static int pointCount() {
        return POINTS.length / 2;
    }
}
