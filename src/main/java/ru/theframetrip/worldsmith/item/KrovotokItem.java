package ru.theframetrip.worldsmith.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class KrovotokItem extends SwordItem {
    public static final String CHARGE_TAG = "KrovotokCharge";
    public static final String LAST_HIT_TAG = "KrovotokLastHitGameTime";
    public static final int MAX_CHARGE = 5;
    private static final int COMBO_TIMEOUT_TICKS = 60;
    private static final float DAMAGE_PER_CHARGE = 0.6F;
    private static final float ATTACK_SPEED_PER_CHARGE = 0.06F;
    private static final float DISCHARGE_BONUS_DAMAGE = 6.0F;
    private static final float MAX_HEAL = 4.0F;
    private static final float HEAL_MULTIPLIER = 0.5F;
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("2ebd5c5b-4ec9-4a83-9100-0ed4c61dde12");

    public KrovotokItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    public static int getCharge(ItemStack stack) {
        return Math.max(0, Math.min(MAX_CHARGE, stack.getOrCreateTag().getInt(CHARGE_TAG)));
    }

    private static void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateTag().putInt(CHARGE_TAG, Math.max(0, Math.min(MAX_CHARGE, charge)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker.level() instanceof ServerLevel serverLevel) {
            handleCrimsonRhythm(stack, target, attacker, serverLevel);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private void handleCrimsonRhythm(ItemStack stack, LivingEntity target, LivingEntity attacker, ServerLevel level) {
        CompoundTag tag = stack.getOrCreateTag();
        long gameTime = level.getGameTime();
        int charge = getCharge(stack);
        long lastHit = tag.getLong(LAST_HIT_TAG);
        if (lastHit > 0L && gameTime - lastHit > COMBO_TIMEOUT_TICKS) {
            charge = 0;
        }

        if (charge >= MAX_CHARGE) {
            float before = target.getHealth();
            boolean damaged = target.hurt(attacker.damageSources().mobAttack(attacker), DISCHARGE_BONUS_DAMAGE);
            float dealt = damaged ? Math.max(0.0F, before - target.getHealth()) : 0.0F;
            if (dealt > 0.0F) {
                attacker.heal(Math.min(MAX_HEAL, dealt * HEAL_MULTIPLIER));
                spawnLifeDrain(level, target, attacker);
            }
            spawnTargetParticles(level, ModParticleTypes.KROVOTOK_BLOOD_BURST.get(), target, 10, 0.35D);
            setCharge(stack, 0);
        } else {
            if (charge > 0) {
                target.hurt(attacker.damageSources().mobAttack(attacker), charge * DAMAGE_PER_CHARGE);
            }
            spawnTargetParticles(level, ModParticleTypes.KROVOTOK_BLOOD_PULSE.get(), target, 3 + charge, 0.18D);
            setCharge(stack, charge + 1);
        }
        tag.putLong(LAST_HIT_TAG, gameTime);
    }

    private static void spawnTargetParticles(ServerLevel level, SimpleParticleType type, LivingEntity target, int count, double spread) {
        level.sendParticles(type, target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(), count, spread, spread * 0.7D, spread, 0.015D);
    }

    private static void spawnLifeDrain(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        Random random = new Random();
        int count = 8 + random.nextInt(7);
        for (int i = 0; i < count; i++) {
            double t = (i + 1.0D) / (count + 1.0D);
            double x = target.getX() + (attacker.getX() - target.getX()) * t;
            double y = target.getY() + target.getBbHeight() * 0.55D + (attacker.getY() + attacker.getBbHeight() * 0.45D - (target.getY() + target.getBbHeight() * 0.55D)) * t;
            double z = target.getZ() + (attacker.getZ() - target.getZ()) * t;
            level.sendParticles(ModParticleTypes.KROVOTOK_LIFE_DRAIN.get(), x, y, z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> base = super.getAttributeModifiers(slot, stack);
        if (slot != EquipmentSlot.MAINHAND) {
            return base;
        }
        int charge = getCharge(stack);
        if (charge <= 0) {
            return base;
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_MODIFIER_ID, "Krovotok charge speed", charge * ATTACK_SPEED_PER_CHARGE, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide || !selected || !(entity instanceof LivingEntity living)) {
            return;
        }
        int charge = getCharge(stack);
        Random random = new Random();
        if (charge == 0 && random.nextInt(45) == 0) {
            level.addParticle(ModParticleTypes.KROVOTOK_BLOOD_MIST.get(), living.getX(), living.getEyeY() - 0.45D, living.getZ(), 0.0D, 0.005D, 0.0D);
        } else if (charge < MAX_CHARGE && random.nextInt(Math.max(8, 22 - charge * 3)) == 0) {
            level.addParticle(ModParticleTypes.KROVOTOK_BLOOD_SPARK.get(), living.getX(), living.getEyeY() - 0.4D, living.getZ(), (random.nextDouble() - 0.5D) * 0.02D, 0.01D, (random.nextDouble() - 0.5D) * 0.02D);
        } else if (charge == MAX_CHARGE && level.getGameTime() % (8 + random.nextInt(5)) == 0) {
            level.addParticle(ModParticleTypes.KROVOTOK_BLOOD_PULSE.get(), living.getX(), living.getEyeY() - 0.4D, living.getZ(), 0.0D, 0.01D, 0.0D);
            level.addParticle(ModParticleTypes.KROVOTOK_BLOOD_SPARK.get(), living.getX(), living.getEyeY() - 0.35D, living.getZ(), 0.0D, 0.015D, 0.0D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.worldsmith.krovotok.ability").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.worldsmith.krovotok.charge", getCharge(stack), MAX_CHARGE).withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.worldsmith.krovotok.description").withStyle(ChatFormatting.GRAY));
    }
}
