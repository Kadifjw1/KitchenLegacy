package ru.theframetrip.worldsmith.ability.prah;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        ArmorStand echo = createEchoStand(player, level, first);

        KNOWN_ECHO_ENTITIES.add(echo.getUUID());
        if (!level.addFreshEntity(echo)) {
            KNOWN_ECHO_ENTITIES.remove(echo.getUUID());
            return false;
        }

        ACTIVE_ECHOES.put(player.getUUID(), new EchoPlayback(player.getUUID(), echo, playbackFrames));
        player.getCooldowns().addCooldown(ModItems.PRAH.get(), COOLDOWN_TICKS);

        level.sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 0.9D, player.getZ(),
                28, 0.45D, 0.85D, 0.45D, 0.025D);
        level.sendParticles(ParticleTypes.SMOKE, first.x(), first.y() + 0.9D, first.z(),
                12, 0.3D, 0.7D, 0.3D, 0.015D);
        player.displayClientMessage(Component.translatable("message.worldsmith.prah.activated"), true);
        return true;
    }

    private static ArmorStand createEchoStand(ServerPlayer owner, ServerLevel level, PrahEchoFrame first) {
        ArmorStand stand = new ArmorStand(level, first.x(), first.y(), first.z());
        stand.setNoGravity(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        stand.setGlowingTag(true);
        stand.setShowArms(true);
        stand.setNoBasePlate(true);
        // Armor stands are full-sized by default. ArmorStand#setSmall is private in 1.20.1 mappings,
        // so no explicit call is needed here.
        stand.setCustomName(Component.translatable("entity.worldsmith.prah_echo"));
        stand.setCustomNameVisible(false);
        stand.getPersistentData().putBoolean(ECHO_ENTITY_TAG, true);

        stand.setItemSlot(EquipmentSlot.MAINHAND, owner.getMainHandItem().copy());
        stand.setItemSlot(EquipmentSlot.OFFHAND, owner.getOffhandItem().copy());
        stand.setItemSlot(EquipmentSlot.CHEST, owner.getItemBySlot(EquipmentSlot.CHEST).copy());
        stand.setItemSlot(EquipmentSlot.LEGS, owner.getItemBySlot(EquipmentSlot.LEGS).copy());
        stand.setItemSlot(EquipmentSlot.FEET, owner.getItemBySlot(EquipmentSlot.FEET).copy());

        ItemStack head = owner.getItemBySlot(EquipmentSlot.HEAD).copy();
        if (head.isEmpty()) {
            head = new ItemStack(Items.PLAYER_HEAD);
            CompoundTag profileTag = new CompoundTag();
            NbtUtils.writeGameProfile(profileTag, owner.getGameProfile());
            head.getOrCreateTag().put("SkullOwner", profileTag);
        }
        stand.setItemSlot(EquipmentSlot.HEAD, head);
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
            if (playback.echo.isRemoved() || playback.frameIndex >= playback.frames.size()) {
                crumble(playback);
                iterator.remove();
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
        double loweredY = frame.y() - (frame.crouching() ? 0.22D : 0.0D);

        echo.setPos(frame.x(), loweredY, frame.z());
        echo.setYRot(frame.yRot());
        echo.setXRot(frame.xRot());
        echo.yRotO = frame.yRot();
        echo.xRotO = frame.xRot();
        echo.setDeltaMovement(Vec3.ZERO);
        echo.setHeadPose(new Rotations(frame.xRot(), 0.0F, 0.0F));
        echo.setBodyPose(new Rotations(frame.crouching() ? 12.0F : 0.0F, 0.0F, 0.0F));

        if (frame.swinging() && !playback.previousSwinging) {
            playback.swingTicks = 8;
            replayAttack(playback, level, frame);
        }
        playback.previousSwinging = frame.swinging();

        updateArmPose(playback);
        triggerBlockInteractions(level, echo);

        if (playback.frameIndex % 10 == 0) {
            distractHostiles(level, echo);
        }

        level.sendParticles(ParticleTypes.ASH, echo.getX(), echo.getY() + 0.9D, echo.getZ(),
                4, 0.24D, 0.75D, 0.24D, 0.008D);
        if (playback.frameIndex % 4 == 0) {
            level.sendParticles(ParticleTypes.SMOKE, echo.getX(), echo.getY() + 0.75D, echo.getZ(),
                    1, 0.12D, 0.35D, 0.12D, 0.005D);
        }

        playback.frameIndex++;
    }

    private static void updateArmPose(EchoPlayback playback) {
        if (playback.swingTicks <= 0) {
            playback.echo.setRightArmPose(new Rotations(-10.0F, 0.0F, 0.0F));
            return;
        }

        float progress = 1.0F - playback.swingTicks / 8.0F;
        float angle = -10.0F - (float) Math.sin(progress * Math.PI) * 115.0F;
        playback.echo.setRightArmPose(new Rotations(angle, 0.0F, 0.0F));
        playback.swingTicks--;
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
        level.sendParticles(ParticleTypes.ASH, target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(),
                14, target.getBbWidth() * 0.45D, target.getBbHeight() * 0.45D,
                target.getBbWidth() * 0.45D, 0.025D);
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
        level.sendParticles(ParticleTypes.ASH, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                42, target.getBbWidth() * 0.7D, target.getBbHeight() * 0.65D,
                target.getBbWidth() * 0.7D, 0.055D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + target.getBbHeight() * 0.45D, target.getZ(),
                12, target.getBbWidth() * 0.45D, target.getBbHeight() * 0.45D,
                target.getBbWidth() * 0.45D, 0.02D);
    }

    private static void distractHostiles(ServerLevel level, ArmorStand echo) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, echo.getBoundingBox().inflate(DISTRACTION_RADIUS),
                candidate -> candidate.isAlive() && candidate instanceof Enemy)) {
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

    private static void crumble(EchoPlayback playback) {
        ArmorStand echo = playback.echo;
        if (echo.level() instanceof ServerLevel level && !echo.isRemoved()) {
            level.sendParticles(ParticleTypes.ASH, echo.getX(), echo.getY() + 0.9D, echo.getZ(),
                    55, 0.5D, 0.9D, 0.5D, 0.06D);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, echo.getX(), echo.getY() + 0.75D, echo.getZ(),
                    16, 0.35D, 0.7D, 0.35D, 0.025D);
            echo.discard();
        }
        KNOWN_ECHO_ENTITIES.remove(echo.getUUID());
    }

    private static void removeEcho(UUID ownerId, boolean playParticles) {
        EchoPlayback existing = ACTIVE_ECHOES.remove(ownerId);
        if (existing == null) {
            return;
        }
        if (playParticles) {
            crumble(existing);
        } else {
            KNOWN_ECHO_ENTITIES.remove(existing.echo.getUUID());
            existing.echo.discard();
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
            playback.echo.discard();
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

        private EchoPlayback(UUID ownerId, ArmorStand echo, List<PrahEchoFrame> frames) {
            this.ownerId = ownerId;
            this.echo = echo;
            this.frames = frames;
        }
    }
}
