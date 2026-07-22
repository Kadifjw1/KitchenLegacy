package ru.theframetrip.worldsmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class PredelItem extends SwordItem {
    public PredelItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.worldsmith.predel.ability").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.worldsmith.predel.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.worldsmith.predel.key").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.worldsmith.predel.cooldown").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide && selected && entity.tickCount % (8 + level.random.nextInt(7)) == 0) {
            level.addParticle(ModParticleTypes.VOID_MOTE.get(), entity.getX(), entity.getEyeY() - 0.35D, entity.getZ(), 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level();
        if (level.isClientSide) {
            int count = 4 + level.random.nextInt(4);
            for (int i = 0; i < count; i++) {
                level.addParticle(ModParticleTypes.VOID_SHARD.get(), entity.getX(), entity.getEyeY() - 0.25D, entity.getZ(),
                        (level.random.nextDouble() - 0.5D) * 0.08D, level.random.nextDouble() * 0.04D, (level.random.nextDouble() - 0.5D) * 0.08D);
            }
        }
        return super.onEntitySwing(stack, entity);
    }
}
