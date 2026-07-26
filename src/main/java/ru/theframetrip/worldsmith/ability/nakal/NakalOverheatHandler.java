package ru.theframetrip.worldsmith.ability.nakal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.worldsmith.WorldsmithMod;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NakalOverheatHandler {
    public static final int MAX_STACKS = 5;

    private static final String STACKS_TAG = "WorldsmithNakalOverheatStacks";
    private static final String LAST_HIT_TAG = "WorldsmithNakalOverheatLastHit";
    private static final String LAST_DECAY_TAG = "WorldsmithNakalOverheatLastDecay";
    private static final String OWNER_TAG = "WorldsmithNakalOverheatOwner";

    private static final int DECAY_DELAY_TICKS = 60;
    private static final int DECAY_INTERVAL_TICKS = 20;
    private static final double ARMOR_REDUCTION_PER_STACK = 0.05D;
    private static final float RUPTURE_DAMAGE = 6.0F;
    private static final int RUPTURE_FIRE_SECONDS = 4;

    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString("44a81750-a71e-46d5-b090-11cb165894df");

    private static final Map<UUID, Set<UUID>> TARGETS_BY_OWNER = new ConcurrentHashMap<>();

    private NakalOverheatHandler() {
    }

    public static void applyHit(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        CompoundTag data = target.getPersistentData();
        UUID owner = attacker.getUUID();

        if (data.hasUUID(OWNER_TAG) && !data.getUUID(OWNER_TAG).equals(owner)) {
            clearTarget(target);
        }

        int stacks = Math.max(0, Math.min(MAX_STACKS, data.getInt(STACKS_TAG)));
        int nextStacks = stacks + 1;

        registerTarget(owner, target.getUUID());

        if (nextStacks >= MAX_STACKS) {
            triggerRupture(level, target, owner);
            return;
        }

        long gameTime = level.getGameTime();
        data.putInt(STACKS_TAG, nextStacks);
        data.putLong(LAST_HIT_TAG, gameTime);
        data.putLong(LAST_DECAY_TAG, gameTime);
        data.putUUID(OWNER_TAG, owner);

        updateArmorModifier(target, nextStacks);
        NakalParticleSpawner.spawnOverheatStack(level, target, nextStacks);
    }

    public static void clearOwner(ServerLevel level, UUID owner) {
        Set<UUID> targetIds = TARGETS_BY_OWNER.remove(owner);
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }

        for (UUID targetId : targetIds) {
            if (level.getEntity(targetId) instanceof LivingEntity target) {
                CompoundTag data = target.getPersistentData();
                if (data.hasUUID(OWNER_TAG) && data.getUUID(OWNER_TAG).equals(owner)) {
                    clearTarget(target);
                }
            }
        }
    }

    private static void triggerRupture(ServerLevel level, LivingEntity target, UUID owner) {
        clearTarget(target);
        unregisterTarget(owner, target.getUUID());

        NakalParticleSpawner.spawnRupture(level, target);
        target.hurt(level.damageSources().onFire(), RUPTURE_DAMAGE);
        target.setSecondsOnFire(RUPTURE_FIRE_SECONDS);
    }

    private static void updateArmorModifier(LivingEntity target, int stacks) {
        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }

        armor.removeModifier(ARMOR_MODIFIER_ID);

        if (stacks > 0) {
            armor.addTransientModifier(new AttributeModifier(
                    ARMOR_MODIFIER_ID,
                    "Nakal overheat armor reduction",
                    -ARMOR_REDUCTION_PER_STACK * stacks,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static void clearTarget(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        UUID owner = data.hasUUID(OWNER_TAG) ? data.getUUID(OWNER_TAG) : null;

        data.remove(STACKS_TAG);
        data.remove(LAST_HIT_TAG);
        data.remove(LAST_DECAY_TAG);
        data.remove(OWNER_TAG);

        updateArmorModifier(target, 0);

        if (owner != null) {
            unregisterTarget(owner, target.getUUID());
        }
    }

    private static void registerTarget(UUID owner, UUID target) {
        TARGETS_BY_OWNER
                .computeIfAbsent(owner, ignored -> ConcurrentHashMap.newKeySet())
                .add(target);
    }

    private static void unregisterTarget(UUID owner, UUID target) {
        Set<UUID> targets = TARGETS_BY_OWNER.get(owner);
        if (targets == null) {
            return;
        }

        targets.remove(target);
        if (targets.isEmpty()) {
            TARGETS_BY_OWNER.remove(owner);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        CompoundTag data = target.getPersistentData();
        int stacks = data.getInt(STACKS_TAG);
        if (stacks <= 0) {
            return;
        }

        long gameTime = level.getGameTime();
        long lastHit = data.getLong(LAST_HIT_TAG);
        long lastDecay = data.getLong(LAST_DECAY_TAG);

        if (gameTime - lastHit < DECAY_DELAY_TICKS || gameTime - lastDecay < DECAY_INTERVAL_TICKS) {
            return;
        }

        int nextStacks = stacks - 1;
        data.putLong(LAST_DECAY_TAG, gameTime);

        if (nextStacks <= 0) {
            clearTarget(target);
            return;
        }

        data.putInt(STACKS_TAG, nextStacks);
        updateArmorModifier(target, nextStacks);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        clearTarget(event.getEntity());
    }
}
