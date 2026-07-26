package ru.theframetrip.worldsmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ru.theframetrip.worldsmith.ability.nakal.NakalOverheatHandler;
import ru.theframetrip.worldsmith.ability.nakal.NakalParticleSpawner;

import java.util.List;

public class NakalItem extends SwordItem {
    private static final String IGNITED_TAG = "NakalIgnited";

    public NakalItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    public static boolean isIgnited(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(IGNITED_TAG);
    }

    private static void setIgnited(ItemStack stack, boolean ignited) {
        if (ignited) {
            stack.getOrCreateTag().putBoolean(IGNITED_TAG, true);
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        tag.remove(IGNITED_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            boolean firstIgnition = !isIgnited(stack);

            // Update both logical sides. The client-side value switches the item-model
            // predicate immediately, while the server remains authoritative.
            setIgnited(stack, true);

            if (entity.level() instanceof ServerLevel serverLevel) {
                if (firstIgnition) {
                    NakalParticleSpawner.spawnIgnitionBurst(serverLevel, player);
                } else {
                    NakalParticleSpawner.spawnSwingBurst(serverLevel, player);
                }
            }
        }

        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isIgnited(stack) && attacker.level() instanceof ServerLevel serverLevel) {
            NakalOverheatHandler.applyHit(serverLevel, target, attacker);
            NakalParticleSpawner.spawnImpactBurst(serverLevel, target);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!(entity instanceof Player player)) {
            return;
        }

        if (!selected && isIgnited(stack)) {
            setIgnited(stack, false);

            if (level instanceof ServerLevel serverLevel) {
                NakalOverheatHandler.clearOwner(serverLevel, player.getUUID());
            }
        }

        // The continuous flame is now an animated model overlay attached directly to
        // the blade. World particles are intentionally not spawned every tick: they
        // cannot follow the first-person item transform and previously appeared near
        // the opposite hand.
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.worldsmith.nakal.ability").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.worldsmith.nakal.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.worldsmith.nakal.ignition").withStyle(ChatFormatting.DARK_GRAY));
    }
}
