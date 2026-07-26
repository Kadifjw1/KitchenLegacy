package ru.theframetrip.worldsmith.ability.nakal;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

public final class NakalParticleSpawner {
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

    private NakalParticleSpawner() {
    }

    public static void spawnBurningBlade(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 base = bladeBase(player);
        Vec3 direction = bladeDirection(player);
        Vec3 right = horizontalRight(player);

        for (int i = 0; i < 5; i++) {
            double t = 0.12D + i * 0.20D;
            Vec3 center = base.add(direction.scale(t));
            double edge = 0.035D + t * 0.03D;

            if (random.nextFloat() < 0.90F) {
                spawnSingle(level, ModParticleTypes.HEAT_FLAME.get(),
                        center.add(right.scale(edge)), randomMotion(random, 0.004D, 0.018D));
            }

            if (random.nextFloat() < 0.90F) {
                spawnSingle(level, ModParticleTypes.HEAT_FLAME.get(),
                        center.add(right.scale(-edge)), randomMotion(random, 0.004D, 0.018D));
            }

            if (random.nextFloat() < 0.10F) {
                spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(),
                        center, randomMotion(random, 0.012D, 0.032D));
            }
        }
    }

    public static void spawnIgnitionBurst(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 base = bladeBase(player);
        Vec3 direction = bladeDirection(player);

        for (int point = 0; point < 7; point++) {
            double t = 0.08D + point * 0.15D;
            Vec3 position = base.add(direction.scale(t));

            for (int i = 0; i < 2; i++) {
                spawnSingle(level, ModParticleTypes.HEAT_FLAME.get(),
                        position, randomMotion(random, 0.018D, 0.040D));
            }

            if (point % 2 == 0) {
                spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(),
                        position, randomMotion(random, 0.025D, 0.055D));
            }
        }

        Vec3 tip = base.add(direction.scale(1.05D));
        for (int i = 0; i < 12; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_SPARK.get(), tip,
                    new Vec3(gaussian(random, 0.12D),
                            0.04D + random.nextDouble() * 0.14D,
                            gaussian(random, 0.12D)));
        }
    }

    public static void spawnSwingBurst(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 base = bladeBase(player);
        Vec3 direction = bladeDirection(player);
        Vec3 right = horizontalRight(player);

        for (int i = 0; i < 9; i++) {
            double t = 0.22D + random.nextDouble() * 0.83D;
            Vec3 position = base.add(direction.scale(t));
            Vec3 velocity = direction.scale(0.08D + random.nextDouble() * 0.11D)
                    .add(right.scale(gaussian(random, 0.10D)))
                    .add(0.0D, 0.025D + random.nextDouble() * 0.09D, 0.0D);
            spawnSingle(level, ModParticleTypes.HEAT_SPARK.get(), position, velocity);
        }

        for (int i = 0; i < 4; i++) {
            double t = 0.30D + random.nextDouble() * 0.70D;
            spawnSingle(level, ModParticleTypes.HEAT_FLAME.get(),
                    base.add(direction.scale(t)), randomMotion(random, 0.016D, 0.034D));
        }
    }

    public static void spawnImpactBurst(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);

        for (int i = 0; i < 13; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_SPARK.get(), center,
                    new Vec3(gaussian(random, 0.18D),
                            0.04D + random.nextDouble() * 0.16D,
                            gaussian(random, 0.18D)));
        }

        for (int i = 0; i < 4; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(),
                    center, randomMotion(random, 0.06D, 0.11D));
        }
    }

    public static void spawnOverheatStack(ServerLevel level, LivingEntity target, int stacks) {
        RandomSource random = level.getRandom();
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        int count = 2 + stacks;

        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count + random.nextDouble() * 0.4D;
            double radius = target.getBbWidth() * 0.42D + 0.08D;
            Vec3 point = center.add(Math.cos(angle) * radius,
                    (random.nextDouble() - 0.5D) * target.getBbHeight() * 0.45D,
                    Math.sin(angle) * radius);

            spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(),
                    point, randomMotion(random, 0.012D, 0.035D));
        }
    }

    public static void spawnRupture(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

        for (int i = 0; i < 28; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_SPARK.get(), center,
                    new Vec3(gaussian(random, 0.26D),
                            0.08D + random.nextDouble() * 0.24D,
                            gaussian(random, 0.26D)));
        }

        for (int i = 0; i < 12; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_FLAME.get(), center,
                    randomMotion(random, 0.12D, 0.18D));
        }

        for (int i = 0; i < 8; i++) {
            spawnSingle(level, ModParticleTypes.HEAT_EMBER.get(), center,
                    randomMotion(random, 0.14D, 0.22D));
        }
    }

    private static Vec3 bladeBase(Player player) {
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 right = horizontalRight(player);

        return player.position()
                .add(0.0D, player.getBbHeight() * 0.72D, 0.0D)
                .add(forward.scale(0.18D))
                .add(right.scale(-0.28D))
                .add(0.0D, -0.12D, 0.0D);
    }

    private static Vec3 bladeDirection(Player player) {
        return player.getLookAngle().normalize()
                .scale(0.76D)
                .add(0.0D, 0.30D, 0.0D)
                .normalize();
    }

    private static Vec3 horizontalRight(Player player) {
        Vec3 right = player.getLookAngle().cross(UP);
        if (right.lengthSqr() < 1.0E-5D) {
            double radians = Math.toRadians(player.getYRot() + 90.0F);
            right = new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
        }
        return right.normalize();
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
}
