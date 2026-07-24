package ru.theframetrip.worldsmith.ability.prah;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.registry.ModItems;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side implementation of the active ability «След их праха».
 */
@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID)
public final class PrahAbilityManager {
    public static final int RECORD_TICKS = 100;
    public static final int COOLDOWN_TICKS = 400;
    public static final int MINIMUM_RECORD_TICKS = 10;
    public static final double DISTRACTION_RADIUS = 10.0D;
    public static final double ATTACK_REACH = 3.0D;
    public static final float ECHO_HIT_DAMAGE = 4.0F;
    public static final float ASH_BURST_DAMAGE = 6.0F;
    public static final int ASH_MARKS_FOR_BURST = 3;
    public static final int ASH_MARK_LIFETIME = 100;

    private static final String ECHO_ENTITY_TAG = "WorldsmithPrahEcho";
    private static final String ASH_MARKS_TAG = "WorldsmithPrahAshMarks";
    private static final String ASH_MARK_UNTIL_TAG = "WorldsmithPrahAshUntil";

    private static final Map<UUID, PlayerHistory> HISTORIES = new HashMap<>();
    private static final Map<UUID, EchoPlayback> ACTIVE_ECHOES = new HashMap<>();
    private static final Set<UUID> KNOWN_ECHO_ENTITIES = new HashSet<>();

    private PrahAbilityManager() {
    }

    public static boolean use(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return false;
        }
        if (!player.getMainHandItem().is(ModItems.PRAH.get())) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(ModItems.PRAH.get())) {
            return false;
        }

        PlayerHistory history = HISTORIES.get(player.getUUID());
        if (history == null || history.frames.size() < MINIMUM_RECORD_TICKS
                || !history.dimension.equals(player.level().dimension())) {
            player.displayClientMessage(Component.translatable("message.worldsmith.prah.not_enough_history"), true);
            return false;
        }

        removeEcho(player.getUUID(), true);

        List<PrahEchoFrame> playbackFrames = new ArrayList<>(history.frames);
        PrahEchoFrame first = playbackFrames.get(0);
        ServerLevel level = player.serverLevel();
        ArmorStand echo = createEchoStand(level, first);

        KNOWN_ECHO_ENTITIES.add(echo.getUUID());
        if (!level.addFreshEntity(echo)) {
            KNOWN_ECHO_ENTITIES.remove(echo.getUUID());
            return false;
        }

        ACTIVE_ECHOES.put(player.getUUID(), new EchoPlayback(player.getUUID(), echo, playbackFrames));
        player.getCooldowns().addCooldown(ModItems.PRAH.get(), COOLDOWN_TICKS);

        level.sendParticles(
                ParticleTypes.ASH,
                player.getX(),
                player.getY() + 0.9D,
                player.getZ(),
                24,
                0.38D,
                0.72D,
                0.38D,
                0.02D
        );
        player.displayClientMessage(Component.translatable("message.worldsmith.prah.activated"), true);
        return true;
    }

    private static ArmorStand createEchoStand(ServerLevel level, PrahEchoFrame first) {
        ArmorStand stand = new ArmorStand(level, first.x(), first.y(), first.z());
        stand.setNoGravity(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        stand.setGlowingTag(false);
        stand.setShowArms(false);
        stand.setNoBasePlate(true);
        stand.setCustomName(Component.translatable("entity.worldsmith.prah_echo"));
        stand.setCustomNameVisible(false);
        stand.getPersistentData().putBoolean(ECHO_ENTITY_TAG, true);
        return stand;
    }

    @SubscribeEvent
    public static void recordPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (!player.isAlive() || player.isSpectator() || !player.getMainHandItem().is(ModItems.PRAH.get())) {
            HISTORIES.remove(player.getUUID());
            return;
        }

        ResourceKey<Level> dimension = player.level().dimension();
        PlayerHistory history = HISTORIES.computeIfAbsent(player.getUUID(), ignored -> new PlayerHistory(dimension));
        if (!history.dimension.equals(dimension)) {
            history = new PlayerHistory(dimension);
            HISTORIES.put(player.getUUID(), history);
        }

        history.frames.addLast(new PrahEchoFrame(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot(),
                player.onGround(),
                player.isShiftKeyDown(),
                player.isSprinting(),
                player.swinging
        ));

        while (history.frames.size() > RECORD_TICKS) {
            history.frames.removeFirst();
        }
    }

    @SubscribeEvent
    public static void tickEchoes(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE_ECHOES.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, EchoPlayback>> iterator = ACTIVE_ECHOES.entrySet().iterator();
        while (iterator.hasNext()) {
            EchoPlayback playback = iterator.next().getValue();

            if (playback.echo.isRemoved()) {
                KNOWN_ECHO_ENTITIES.remove(playback.echo.getUUID());
                iterator.remove();
                continue;
            }

            if (playback.crumbling) {
                if (tickCrumble(playback)) {
                    iterator.remove();
                }
                continue;
            }

            if (playback.frameIndex >= playback.frames.size()) {
                playback.crumbling = true;
                playback.crumbleTicks = 0;
                continue;
            }

            tickPlayback(playback);
        }
    }

    private static void tickPlayback(EchoPlayback playback) {
        ArmorStand echo = playback.echo;
        if (!(echo.level() instanceof ServerLevel level)) {
            return;
        }

        PrahEchoFrame frame = playback.frames.get(playback.frameIndex);
        PrahEchoFrame previousFrame = playback.frameIndex > 0
                ? playback.frames.get(playback.frameIndex - 1)
                : null;

        echo.setPos(frame.x(), frame.y(), frame.z());
        echo.setYRot(frame.yRot());
        echo.setXRot(frame.xRot());
        echo.yRotO = frame.yRot();
        echo.xRotO = frame.xRot();
        echo.setDeltaMovement(Vec3.ZERO);

        if (frame.swinging() && !playback.previousSwinging) {
            playback.swingTicks = 8;
            replayAttack(playback, level, frame);
        }
        playback.previousSwinging = frame.swinging();

        triggerBlockInteractions(level, echo);

        if (playback.frameIndex % 10 == 0) {
            distractHostiles(level, echo);
        }

        if (playback.frameIndex % 2 == 0) {
            PrahEchoParticleRenderer.render(
                    level,
                    echo,
                    frame,
                    previousFrame,
                    playback.swingTicks
            );
        }

        if (playback.swingTicks > 0) {
            playback.swingTicks--;
        }
        playback.frameIndex++;
    }

    private static boolean tickCrumble(EchoPlayback playback) {
        ArmorStand echo = playback.echo;
        if (!(echo.level() instanceof ServerLevel level)) {
            discardEcho(playback, false);
            return true;
        }

        PrahEchoFrame lastFrame = playback.frames.get(playback.frames.size() - 1);
        boolean finished = PrahEchoParticleRenderer.renderCrumble(
                level,
                echo,
                lastFrame,
                playback.crumbleTicks
        );
        playback.crumbleTicks++;

        if (finished) {
            discardEcho(playback, false);
            return true;
        }
        return false;
    }

    private static void replayAttack(EchoPlayback playback, ServerLevel level, PrahEchoFrame frame) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(playback.ownerId);
        if (owner == null || owner.serverLevel() != level) {
            return;
        }

        ArmorStand echo = playback.echo;
        Vec3 look = Vec3.directionFromRotation(frame.xRot(), frame.yRot()).normalize();
        AABB searchBox = echo.getBoundingBox().expandTowards(look.scale(ATTACK_REACH)).inflate(0.9D);

        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, searchBox, candidate -> {
                    if (!candidate.isAlive() || candidate == owner || candidate == echo || candidate instanceof ArmorStand) {
                        return false;
                    }
                    if (owner.isAlliedTo(candidate)) {
                        return false;
                    }
                    Vec3 toTarget = candidate.getEyePosition().subtract(echo.getEyePosition());
                    return toTarget.lengthSqr() > 0.0001D && look.dot(toTarget.normalize()) > 0.2D;
                }).stream()
                .min(Comparator.comparingDouble(echo::distanceToSqr))
                .orElse(null);

        if (target == null) {
            return;
        }

        if (!target.hurt(level.damageSources().playerAttack(owner), ECHO_HIT_DAMAGE)) {
            return;
        }

        applyAshMark(level, target);
        level.sendParticles(
                ParticleTypes.ASH,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.55D,
                target.getZ(),
                14,
                target.getBbWidth() * 0.45D,
                target.getBbHeight() * 0.45D,
                target.getBbWidth() * 0.45D,
                0.025D
        );
    }

    private static void applyAshMark(ServerLevel level, LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        long now = level.getGameTime();
        int marks = data.getLong(ASH_MARK_UNTIL_TAG) < now ? 0 : data.getInt(ASH_MARKS_TAG);
        marks++;

        data.putInt(ASH_MARKS_TAG, marks);
        data.putLong(ASH_MARK_UNTIL_TAG, now + ASH_MARK_LIFETIME);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));

        if (marks < ASH_MARKS_FOR_BURST) {
            return;
        }

        data.remove(ASH_MARKS_TAG);
        data.remove(ASH_MARK_UNTIL_TAG);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0));
        target.hurt(level.damageSources().magic(), ASH_BURST_DAMAGE);
        level.sendParticles(
                ParticleTypes.ASH,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                42,
                target.getBbWidth() * 0.7D,
                target.getBbHeight() * 0.65D,
                target.getBbWidth() * 0.7D,
                0.055D
        );
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.45D,
                target.getZ(),
                12,
                target.getBbWidth() * 0.45D,
                target.getBbHeight() * 0.45D,
                target.getBbWidth() * 0.45D,
                0.02D
        );
    }

    private static void distractHostiles(ServerLevel level, ArmorStand echo) {
        for (Mob mob : level.getEntitiesOfClass(
                Mob.class,
                echo.getBoundingBox().inflate(DISTRACTION_RADIUS),
                candidate -> candidate.isAlive() && candidate instanceof Enemy
        )) {
            mob.setTarget(echo);
        }
    }

    private static void triggerBlockInteractions(ServerLevel level, ArmorStand echo) {
        triggerBlock(level, echo.blockPosition(), echo);
        triggerBlock(level, echo.blockPosition().below(), echo);
    }

    private static void triggerBlock(ServerLevel level, BlockPos pos, ArmorStand echo) {
        if (level.hasChunkAt(pos)) {
            level.getBlockState(pos).entityInside(level, pos, echo);
        }
    }

    private static void discardEcho(EchoPlayback playback, boolean playParticles) {
        ArmorStand echo = playback.echo;
        if (echo.level() instanceof ServerLevel level && !echo.isRemoved() && playParticles) {
            PrahEchoParticleRenderer.spawnImmediateDissolve(level, echo);
        }
        KNOWN_ECHO_ENTITIES.remove(echo.getUUID());
        if (!echo.isRemoved()) {
            echo.discard();
        }
    }

    private static void removeEcho(UUID ownerId, boolean playParticles) {
        EchoPlayback existing = ACTIVE_ECHOES.remove(ownerId);
        if (existing != null) {
            discardEcho(existing, playParticles);
        }
    }

    @SubscribeEvent
    public static void removeOrphanedEcho(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ArmorStand stand)) {
            return;
        }
        if (stand.getPersistentData().getBoolean(ECHO_ENTITY_TAG)
                && !KNOWN_ECHO_ENTITIES.contains(stand.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        HISTORIES.remove(playerId);
        removeEcho(playerId, false);
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event) {
        for (EchoPlayback playback : ACTIVE_ECHOES.values()) {
            discardEcho(playback, false);
        }
        HISTORIES.clear();
        ACTIVE_ECHOES.clear();
        KNOWN_ECHO_ENTITIES.clear();
    }

    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(ModItems.PRAH.get())) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.worldsmith.prah.ability")
                .withStyle(ChatFormatting.DARK_GRAY));
        event.getToolTip().add(Component.translatable("tooltip.worldsmith.prah.description")
                .withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable("tooltip.worldsmith.prah.key")
                .withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.translatable("tooltip.worldsmith.prah.cooldown")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static final class PlayerHistory {
        private final ResourceKey<Level> dimension;
        private final Deque<PrahEchoFrame> frames = new ArrayDeque<>();

        private PlayerHistory(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }
    }

    private static final class EchoPlayback {
        private final UUID ownerId;
        private final ArmorStand echo;
        private final List<PrahEchoFrame> frames;
        private int frameIndex;
        private boolean previousSwinging;
        private int swingTicks;
        private boolean crumbling;
        private int crumbleTicks;

        private EchoPlayback(UUID ownerId, ArmorStand echo, List<PrahEchoFrame> frames) {
            this.ownerId = ownerId;
            this.echo = echo;
            this.frames = frames;
        }
    }
}
