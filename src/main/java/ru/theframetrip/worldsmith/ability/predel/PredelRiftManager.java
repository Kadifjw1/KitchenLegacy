package ru.theframetrip.worldsmith.ability.predel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.event.PredelRiftPermissionEvent;
import ru.theframetrip.worldsmith.registry.ModBlocks;
import ru.theframetrip.worldsmith.registry.ModItems;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID)
public class PredelRiftManager {
    public static final int DURATION_TICKS = 80;
    public static final int COOLDOWN_TICKS = 240;
    public static final int SAFE_POSITION_SEARCH_RADIUS = 3;
    public static final double RANGE_BLOCKS = 8.0D;
    public static final double FALLBACK_DAMAGE = 2.0D;

    private static final ResourceLocation IMMUNE_TAG = new ResourceLocation(WorldsmithMod.MOD_ID, "rift_immune");
    private static final Map<ResourceKey<Level>, Map<BlockPos, UUID>> OWNED_POSITIONS = new HashMap<>();

    public static boolean use(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return false;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(ModItems.PREDEL.get()) || player.getCooldowns().isOnCooldown(ModItems.PREDEL.get())) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 target = eye.add(player.getViewVector(1.0F).scale(RANGE_BLOCKS));
        BlockHitResult hit = level.clip(new ClipContext(eye, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || !level.hasChunkAt(hit.getBlockPos())) {
            return false;
        }

        List<BlockPos> validPositions = new ArrayList<>();
        for (BlockPos candidate : buildPlane(hit.getBlockPos(), hit.getDirection())) {
            if (canPhase(player, level, candidate)) {
                validPositions.add(candidate.immutable());
            }
        }

        if (validPositions.isEmpty()) {
            return false;
        }

        createRift(player, level, validPositions);
        player.getCooldowns().addCooldown(ModItems.PREDEL.get(), COOLDOWN_TICKS);
        return true;
    }

    private static List<BlockPos> buildPlane(BlockPos center, Direction hitFace) {
        List<BlockPos> positions = new ArrayList<>(9);
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                if (hitFace.getAxis() == Direction.Axis.X) {
                    positions.add(center.offset(0, second, first));
                } else if (hitFace.getAxis() == Direction.Axis.Z) {
                    positions.add(center.offset(first, second, 0));
                } else {
                    positions.add(center.offset(first, 0, second));
                }
            }
        }
        return positions;
    }

    private static boolean canPhase(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || isOwned(level.dimension(), pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty() || level.getBlockEntity(pos) != null) {
            return false;
        }
        if (state.is(BlockTags.create(IMMUNE_TAG))) {
            return false;
        }

        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block instanceof BaseEntityBlock
                || block instanceof DoorBlock || block instanceof BedBlock || block instanceof DoublePlantBlock) {
            return false;
        }

        String blockPath = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (blockPath.contains("portal") || blockPath.contains("command") || blockPath.contains("structure")
                || blockPath.contains("jigsaw") || blockPath.contains("piston_head") || blockPath.contains("moving_piston")) {
            return false;
        }

        if (!player.mayUseItemAt(pos, Direction.UP, player.getMainHandItem())) {
            return false;
        }

        return !MinecraftForge.EVENT_BUS.post(new PredelRiftPermissionEvent(player, level, pos, state));
    }

    private static void createRift(ServerPlayer player, ServerLevel level, List<BlockPos> positions) {
        UUID riftId = UUID.randomUUID();
        List<RiftBlockSnapshot> snapshots = new ArrayList<>(positions.size());

        for (BlockPos pos : positions) {
            snapshots.add(new RiftBlockSnapshot(pos, level.getBlockState(pos)));
            own(level.dimension(), pos, riftId);
            level.setBlock(pos, ModBlocks.VOID_PHASE_BLOCK.get().defaultBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
            sendRiftParticles(level, pos);
        }

        RiftInstance rift = new RiftInstance(riftId, player.getUUID(), level.dimension(), snapshots,
                level.getGameTime(), level.getGameTime() + DURATION_TICKS);
        PredelRiftSavedData data = data(level);
        data.rifts.add(rift);
        data.setDirty();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            restoreDue(level);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            restoreDue(level);
        }
    }

    private static void restoreDue(ServerLevel level) {
        PredelRiftSavedData data = data(level);
        boolean dirty = false;
        Iterator<RiftInstance> iterator = data.rifts.iterator();
        List<RiftInstance> pending = new ArrayList<>();

        while (iterator.hasNext()) {
            RiftInstance rift = iterator.next();
            if (!rift.dimension().equals(level.dimension()) || level.getGameTime() < rift.restoreGameTime()) {
                continue;
            }

            List<RiftBlockSnapshot> unresolved = new ArrayList<>();
            for (RiftBlockSnapshot snapshot : rift.blocks()) {
                BlockPos pos = snapshot.position();
                if (!level.hasChunkAt(pos)) {
                    unresolved.add(snapshot);
                    continue;
                }

                UUID currentOwner = owner(level.dimension(), pos);
                if (currentOwner != null && !Objects.equals(currentOwner, rift.riftId())) {
                    continue;
                }

                release(level.dimension(), pos);
                if (level.getBlockState(pos).is(ModBlocks.VOID_PHASE_BLOCK.get())) {
                    evacuateIntersectingEntities(level, pos);
                    level.setBlock(pos, snapshot.originalState(),
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                    sendRiftParticles(level, pos);
                } else {
                    WorldsmithMod.LOGGER.warn("Predel rift service block at {} was replaced; not overwriting", pos);
                }
                dirty = true;
            }

            iterator.remove();
            if (!unresolved.isEmpty()) {
                pending.add(new RiftInstance(rift.riftId(), rift.ownerPlayerId(), rift.dimension(), unresolved,
                        rift.createdGameTime(), 0L));
            }
        }

        if (!pending.isEmpty()) {
            data.rifts.addAll(pending);
            dirty = true;
        }
        if (dirty) {
            data.setDirty();
        }
    }

    private static void evacuateIntersectingEntities(ServerLevel level, BlockPos pos) {
        AABB restoringBlock = new AABB(pos);
        for (Entity entity : level.getEntities((Entity) null, restoringBlock, Entity::isAlive)) {
            Vec3 safePosition = findSafePosition(level, entity, pos);
            entity.teleportTo(safePosition.x, safePosition.y, safePosition.z);
            if (safePosition.y > pos.getY() + SAFE_POSITION_SEARCH_RADIUS) {
                entity.hurt(level.damageSources().magic(), (float) FALLBACK_DAMAGE);
                Vec3 push = entity.position().subtract(Vec3.atCenterOf(pos)).normalize().scale(0.25D);
                entity.push(push.x, 0.15D, push.z);
            }
        }
    }

    private static Vec3 findSafePosition(ServerLevel level, Entity entity, BlockPos center) {
        for (int radius = 1; radius <= SAFE_POSITION_SEARCH_RADIUS; radius++) {
            for (BlockPos candidate : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
                if (!candidate.equals(center) && isSafeDestination(level, entity, candidate)) {
                    return Vec3.atBottomCenterOf(candidate);
                }
            }
        }

        BlockPos fallback = center.above(SAFE_POSITION_SEARCH_RADIUS + 1);
        while (fallback.getY() < level.getMaxBuildHeight() - 1 && !isSafeDestination(level, entity, fallback)) {
            fallback = fallback.above();
        }
        return Vec3.atBottomCenterOf(fallback);
    }

    private static boolean isSafeDestination(ServerLevel level, Entity entity, BlockPos feetPos) {
        if (feetPos.getY() <= level.getMinBuildHeight() || feetPos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }

        AABB movedBox = entity.getBoundingBox().move(Vec3.atBottomCenterOf(feetPos).subtract(entity.position()));
        if (!level.noCollision(entity, movedBox)) {
            return false;
        }

        for (BlockPos occupied : BlockPos.betweenClosed(
                (int) Math.floor(movedBox.minX), (int) Math.floor(movedBox.minY), (int) Math.floor(movedBox.minZ),
                (int) Math.floor(movedBox.maxX - 1.0E-7D), (int) Math.floor(movedBox.maxY - 1.0E-7D), (int) Math.floor(movedBox.maxZ - 1.0E-7D))) {
            if (isImmediateHazard(level, occupied)) {
                return false;
            }
        }

        if (entity.onGround() || !entity.isNoGravity()) {
            BlockPos supportPos = feetPos.below();
            if (supportPos.getY() < level.getMinBuildHeight() || isImmediateHazard(level, supportPos)) {
                return false;
            }
            BlockState support = level.getBlockState(supportPos);
            if (support.getCollisionShape(level, supportPos).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static boolean isImmediateHazard(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight()) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        return !state.getFluidState().isEmpty()
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.LAVA)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.POWDER_SNOW);
    }

    private static PredelRiftSavedData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PredelRiftSavedData::load, PredelRiftSavedData::new, PredelRiftSavedData.NAME);
    }

    private static void sendRiftParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ModParticleTypes.VOID_RIFT.get(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                12, 0.55D, 0.55D, 0.55D, 0.02D);
    }

    private static Map<BlockPos, UUID> ownedPositions(ResourceKey<Level> dimension) {
        return OWNED_POSITIONS.computeIfAbsent(dimension, ignored -> new HashMap<>());
    }

    private static boolean isOwned(ResourceKey<Level> dimension, BlockPos pos) {
        return ownedPositions(dimension).containsKey(pos);
    }

    private static UUID owner(ResourceKey<Level> dimension, BlockPos pos) {
        return ownedPositions(dimension).get(pos);
    }

    private static void own(ResourceKey<Level> dimension, BlockPos pos, UUID riftId) {
        ownedPositions(dimension).put(pos.immutable(), riftId);
    }

    private static void release(ResourceKey<Level> dimension, BlockPos pos) {
        ownedPositions(dimension).remove(pos);
    }
}
