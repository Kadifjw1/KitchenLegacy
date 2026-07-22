package ru.theframetrip.kitchenlegacy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PredelItem extends SwordItem {
    public PredelItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) { super(tier, attackDamageModifier, attackSpeedModifier, properties); }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kitchenlegacy.predel.ability").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.kitchenlegacy.predel.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.kitchenlegacy.predel.key").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.kitchenlegacy.predel.cooldown").withStyle(ChatFormatting.DARK_GRAY));
    }
}
