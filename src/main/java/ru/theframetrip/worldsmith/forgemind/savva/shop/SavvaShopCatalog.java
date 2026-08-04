package ru.theframetrip.worldsmith.forgemind.savva.shop;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class SavvaShopCatalog {
    public enum Direction {
        SELL_TO_PLAYER,
        BUY_FROM_PLAYER
    }

    public record Offer(
            String id,
            Direction direction,
            ResourceLocation itemId,
            int itemCount,
            int currencyCount,
            int maxUses
    ) {
        public Item item() {
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            return item == null ? Items.AIR : item;
        }
    }

    public static final String ROLE_TAG = "WorldsmithRole";
    public static final String ROLE_ID = "savva_produce_vendor";
    public static final String LAST_RESTOCK_DAY_TAG = "WorldsmithSavvaLastRestockDay";
    public static final String TITLE = "Лавка Саввы";
    public static final String BUY_LABEL = "Купить";
    public static final String SELL_LABEL = "Продать";
    public static final int OPEN_TIME = 1000;
    public static final int CLOSE_TIME = 12000;
    public static final int SCREEN_WIDTH = 340;
    public static final int SCREEN_HEIGHT = 220;
    public static final int VISIBLE_ROWS = 7;
    public static final String CURRENCY_ID = "worldsmith:savva_coin";
    public static final String FALLBACK_CURRENCY_ID = "minecraft:emerald";

    public static final List<Offer> OFFERS = List.of(
            new Offer("sell_tomato", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:tomato"), 6, 3, 16),
            new Offer("sell_lettuce", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:lettuce"), 4, 2, 16),
            new Offer("sell_cucumber", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:cucumber"), 6, 3, 16),
            new Offer("sell_strawberry", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:strawberry"), 6, 3, 16),
            new Offer("sell_blueberry", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:blueberry"), 8, 3, 16),
            new Offer("sell_raspberry", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:raspberry"), 6, 3, 16),
            new Offer("sell_pear", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:pear"), 4, 4, 12),
            new Offer("sell_peach", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:peach"), 4, 4, 12),
            new Offer("sell_orange", Direction.SELL_TO_PLAYER, new ResourceLocation("worldsmith:orange"), 4, 4, 12),
            new Offer("sell_carrots", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:carrot"), 8, 2, 16),
            new Offer("sell_potatoes", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:potato"), 8, 2, 16),
            new Offer("sell_beetroot", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:beetroot"), 8, 3, 14),
            new Offer("sell_pumpkin", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:pumpkin"), 1, 4, 8),
            new Offer("sell_apples", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:apple"), 4, 3, 12),
            new Offer("sell_melon", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:melon_slice"), 8, 2, 16),
            new Offer("sell_sweet_berries", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:sweet_berries"), 8, 2, 14),
            new Offer("sell_glow_berries", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:glow_berries"), 4, 3, 10),
            new Offer("sell_beetroot_seeds", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:beetroot_seeds"), 8, 1, 20),
            new Offer("sell_pumpkin_seeds", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:pumpkin_seeds"), 4, 2, 16),
            new Offer("sell_melon_seeds", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:melon_seeds"), 4, 2, 16),
            new Offer("sell_oak_sapling", Direction.SELL_TO_PLAYER, new ResourceLocation("minecraft:oak_sapling"), 2, 3, 8),
            new Offer("buy_tomato", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:tomato"), 12, 3, 12),
            new Offer("buy_lettuce", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:lettuce"), 10, 2, 12),
            new Offer("buy_cucumber", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:cucumber"), 12, 3, 12),
            new Offer("buy_strawberry", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:strawberry"), 12, 3, 12),
            new Offer("buy_blueberry", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:blueberry"), 16, 3, 12),
            new Offer("buy_raspberry", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:raspberry"), 12, 3, 12),
            new Offer("buy_pear", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:pear"), 8, 4, 10),
            new Offer("buy_peach", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:peach"), 8, 4, 10),
            new Offer("buy_orange", Direction.BUY_FROM_PLAYER, new ResourceLocation("worldsmith:orange"), 8, 4, 10),
            new Offer("buy_carrots", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:carrot"), 16, 2, 12),
            new Offer("buy_potatoes", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:potato"), 16, 2, 12),
            new Offer("buy_beetroot", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:beetroot"), 16, 3, 10),
            new Offer("buy_pumpkins", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:pumpkin"), 4, 3, 8),
            new Offer("buy_apples", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:apple"), 8, 3, 10),
            new Offer("buy_sweet_berries", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:sweet_berries"), 16, 3, 10),
            new Offer("buy_glow_berries", Direction.BUY_FROM_PLAYER, new ResourceLocation("minecraft:glow_berries"), 8, 3, 8)
    );

    private SavvaShopCatalog() {
    }

    public static boolean isOpen(long dayTime) {
        long normalized = dayTime % 24000L;
        return normalized >= OPEN_TIME && normalized < CLOSE_TIME;
    }

    public static Item currency() {
        Item preferred = resolveItem(CURRENCY_ID);
        if (preferred != Items.AIR) {
            return preferred;
        }
        Item fallback = resolveItem(FALLBACK_CURRENCY_ID);
        return fallback == Items.AIR ? Items.EMERALD : fallback;
    }

    public static Item resolveItem(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? Items.AIR : item;
    }
}
