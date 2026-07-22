package ru.theframetrip.worldsmith.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class WorldsmithSwordItem extends SwordItem {
    public WorldsmithSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }
}
