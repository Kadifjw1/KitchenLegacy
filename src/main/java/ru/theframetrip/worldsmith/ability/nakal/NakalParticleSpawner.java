package ru.theframetrip.worldsmith.ability.nakal;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

public final class NakalParticleSpawner {
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

    private NakalParticleSpawner() {
    }

    public static void spawnIgnitionBurst(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 base = bladeBase(player);
        Vec3 direction = bladeDirection(player);

        for (int point = 0; point < 5; point++) {
            double t = 0.12D + point * 0.19D;
            Vec3 position = base.add(direction.scale(t));

            if (point % 2 == 0) {
                spawnSingleExceptOwner(level, player, ModParticleTypes.HEAT_FLAME.get(),
                        position, randomMotion(random, 0.008D, 0.020D));
            }

            if (point == 2 || point == 4) {
                spawnSingleExceptOwner(level, player, ModParticleTypes.HEAT_EMBER.get(),
                        position, randomMotion(random, 0.014D, 0.030D));
            }
        }

        Vec3 tip = base.add(direction.scale(1.02D));
        for (int i = 0; i < 6; i++) {
            spawnSingleExceptOwner(level, player, ModParticleTypes.HEAT_SPARK.get(), tip,
                    new Vec3(
                            gaussian(random, 0.075D),
                            0.025D + random.nextDouble() * 0.080D,
                            gaussian(random, 0.075D)
                    ));
        }
    }

    public static void spawnSwingBurst(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 base = bladeBase(player);
        Vec3 direction = bladeDirection(player);
        Vec3 right = horizontalRight(player);
        double handSign = handSign(player);

        for (int i = 0; i < 6; i++) {
            double t = 0.24D + random.nextDouble() * 0.78D;
            Vec3 position = base.add(direction.scale(t));
            Vec3 velocity = direction.scale(0.045D + random.nextDouble() * 0.075D)
                    .add(right.scale(handSign * gaussian(random, 0.050D)))
                    .add(0.0D, 0.015D + random.nextDouble() * 0.055D, 0.0D);

            spawnSingleExceptOwner(level, player, ModParticleTypes.HEAT_SPARK.get(), position, velocity);
        }

        for (int i = 0; i < 2; i++) {
            double t = 0.42D + random.nextDouble() * 0.50D;
            spawnSingleExceptOwner(level, player, ModParticleTypes.HEAT_FLAME.get(),
                    base.add(direction.scale(t)), randomMotion(random, 0.008D, 0.020D));
        }
    }

    public static void spawnImpactBurst(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);

        for (int i = 0; i < 8; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_SPARK.get(), center,
                    new Vec3(
                            gaussian(random, 0.12D),
                            0.025D + random.nextDouble() * 0.10D,
                            gaussian(random, 0.12D)
                    ));
        }

        for (int i = 0; i < 2; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(),
                    center, randomMotion(random, 0.035D, 0.065D));
        }
    }

    public static void spawnOverheatStack(ServerLevel level, LivingEntity target, int stacks) {
        RandomSource random = level.getRandom();
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        int count = 1 + Math.max(1, stacks / 2);

        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count + random.nextDouble() * 0.4D;
            double radius = target.getBbWidth() * 0.40D + 0.06D;
            Vec3 point = center.add(
                    Math.cos(angle) * radius,
                    (random.nextDouble() - 0.5D) * target.getBbHeight() * 0.38D,
                    Math.sin(angle) * radius
            );

            spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(),
                    point, randomMotion(random, 0.008D, 0.024D));
        }
    }

    public static void spawnRupture(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

        for (int i = 0; i < 16; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_SPARK.get(), center,
                    new Vec3(
                            gaussian(random, 0.19D),
                            0.05D + random.nextDouble() * 0.17D,
                            gaussian(random, 0.19D)
                    ));
        }

        for (int i = 0; i < 6; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_FLAME.get(), center,
                    randomMotion(random, 0.075D, 0.12D));
        }

        for (int i = 0; i < 4; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(), center,
                    randomMotion(random, 0.085D, 0.14D));
        }
    }

    private static Vec3 bladeBase(Player player) {
        Vec3 forward = horizontalForward(player);
        Vec3 right = horizontalRight(player);
        double handSign = handSign(player);

        return player.position()
                .add(0.0D, player.getBbHeight() * 0.60D, 0.0D)
                .add(forward.scale(0.16D))
                .add(right.scale(handSign * 0.34D));
    }

    private static Vec3 bladeDirection(Player player) {
        Vec3 forward = horizontalForward(player);
        Vec3 right = horizontalRight(player);
        double handSign = handSign(player);

        return forward.scale(0.38D)
                .add(right.scale(handSign * 0.76D))
                .add(0.0D, 0.20D, 0.0D)
                .normalize();
    }

    private static Vec3 horizontalForward(Player player) {
        double radians = Math.toRadians(player.yBodyRot);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians)).normalize();
    }

    private static Vec3 horizontalRight(Player player) {
        Vec3 right = horizontalForward(player).cross(UP);
        if (right.lengthSqr() < 1.0E-5D) {
            return new Vec3(1.0D, 0.0D, 0.0D);
        }
        return right.normalize();
    }

    private static double handSign(Player player) {
        return player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;
    }

    private static Vec3 randomMotion(RandomSource random, double horizontal, double vertical) {
        return new Vec3(
                gaussian(random, horizontal),
                random.nextDouble() * vertical,
                gaussian(random, horizontal)
        );
    }

    private static double gaussian(RandomSource random, double scale) {
        return random.nextGaussian() * scale;
    }

    private static void spawnSingle(ServerLevel level, ParticleOptions particle, Vec3 position, Vec3 velocity) {
        level.sendParticles(
                particle,
                position.x,
                position.y,
                position.z,
                0,
                velocity.x,
                velocity.y,
                velocity.z,
                1.0D
        );
    }

    private static void spawnSingleExceptOwner(
            ServerLevel level,
            Player owner,
            ParticleOptions particle,
            Vec3 position,
            Vec3 velocity
    ) {
        for (ServerPlayer observer : level.players()) {
            if (observer.getUUID().equals(owner.getUUID())) {
                continue;
            }

            level.sendParticles(
                    observer,
                    particle,
                    false,
                    position.x,
                    position.y,
                    position.z,
                    0,
                    velocity.x,
                    velocity.y,
                    velocity.z,
                    1.0D
            );
        }
    }
}
