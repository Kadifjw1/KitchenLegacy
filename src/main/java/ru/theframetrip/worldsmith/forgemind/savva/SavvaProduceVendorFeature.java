package ru.theframetrip.worldsmith.forgemind.savva;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import ru.theframetrip.worldsmith.WorldsmithMod;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SavvaProduceVendorFeature {
    private static final String ROLE_TAG = "WorldsmithRole";
    private static final String ROLE_ID = "savva_produce_vendor";
    private static final String PROFILE_VERSION_TAG = "WorldsmithSavvaProfileVersion";
    private static final String LAST_RESTOCK_DAY_TAG = "WorldsmithSavvaLastRestockDay";
    private static final int PROFILE_VERSION = 3;
    private static final int OPEN_TIME = 1000;
    private static final int CLOSE_TIME = 12000;
    private static final String DISPLAY_NAME = "Савва Урожайник";
    private static final String CURRENCY_ID = "worldsmith:savva_coin";
    private static final String FALLBACK_CURRENCY_ID = "minecraft:emerald";

    private SavvaProduceVendorFeature() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("worldsmith")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("savva")
                                .then(Commands.literal("spawn")
                                        .executes(context -> spawnSavva(context.getSource()))))
        );
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getTarget() instanceof Villager villager)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ROLE_ID.equals(villager.getPersistentData().getString(ROLE_TAG))) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        long dayTime = player.serverLevel().getDayTime() % 24000L;
        if (dayTime < OPEN_TIME || dayTime >= CLOSE_TIME) {
            player.displayClientMessage(
                    Component.literal("На сегодня лавка закрыта. Урожаю тоже нужен отдых."),
                    true
            );
            return;
        }

        ensureProfile(villager, player.serverLevel());
        villager.interact(player, InteractionHand.MAIN_HAND);
    }

    private static int spawnSavva(CommandSourceStack source) {
        if (!spawnSavva(source.getLevel(), source.getPosition())) {
            source.sendFailure(Component.literal("Не удалось создать Савву."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Савва Урожайник создан."), true);
        return 1;
    }

    public static boolean spawnSavva(ServerLevel level, Vec3 position) {
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            return false;
        }

        villager.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
        villager.setVillagerData(new VillagerData(
                VillagerType.PLAINS,
                VillagerProfession.FARMER,
                5
        ));
        villager.setCustomName(Component.literal(DISPLAY_NAME));
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.setNoAi(true);
        villager.getPersistentData().putString(ROLE_TAG, ROLE_ID);
        villager.getPersistentData().putInt(PROFILE_VERSION_TAG, 0);
        ensureProfile(villager, level);
        return level.addFreshEntity(villager);
    }

    private static void ensureProfile(Villager villager, ServerLevel level) {
        int currentVersion = villager.getPersistentData().getInt(PROFILE_VERSION_TAG);
        if (currentVersion != PROFILE_VERSION || villager.getOffers().isEmpty()) {
            installOffers(villager);
            villager.getPersistentData().putInt(PROFILE_VERSION_TAG, PROFILE_VERSION);
            villager.getPersistentData().putLong(
                    LAST_RESTOCK_DAY_TAG,
                    level.getDayTime() / 24000L
            );
            return;
        }

        long day = level.getDayTime() / 24000L;
        long previousDay = villager.getPersistentData().getLong(LAST_RESTOCK_DAY_TAG);
        if (day != previousDay) {
            for (MerchantOffer offer : villager.getOffers()) {
                offer.resetUses();
            }
            villager.getPersistentData().putLong(LAST_RESTOCK_DAY_TAG, day);
        }
    }

    private static void installOffers(Villager villager) {
        Item currency = resolveItem(CURRENCY_ID);
        if (currency == Items.AIR) {
            currency = resolveItem(FALLBACK_CURRENCY_ID);
        }
        if (currency == Items.AIR) {
            currency = Items.EMERALD;
        }

        MerchantOffers offers = villager.getOffers();
        offers.clear();
        sellToPlayer(offers, currency, "worldsmith:tomato", 6, 3, 16);
        sellToPlayer(offers, currency, "worldsmith:lettuce", 4, 2, 16);
        sellToPlayer(offers, currency, "worldsmith:cucumber", 6, 3, 16);
        sellToPlayer(offers, currency, "worldsmith:strawberry", 6, 3, 16);
        sellToPlayer(offers, currency, "worldsmith:blueberry", 8, 3, 16);
        sellToPlayer(offers, currency, "worldsmith:raspberry", 6, 3, 16);
        sellToPlayer(offers, currency, "worldsmith:pear", 4, 4, 12);
        sellToPlayer(offers, currency, "worldsmith:peach", 4, 4, 12);
        sellToPlayer(offers, currency, "worldsmith:orange", 4, 4, 12);
        sellToPlayer(offers, currency, "minecraft:carrot", 8, 2, 16);
        sellToPlayer(offers, currency, "minecraft:potato", 8, 2, 16);
        sellToPlayer(offers, currency, "minecraft:beetroot", 8, 3, 14);
        sellToPlayer(offers, currency, "minecraft:pumpkin", 1, 4, 8);
        sellToPlayer(offers, currency, "minecraft:apple", 4, 3, 12);
        sellToPlayer(offers, currency, "minecraft:melon_slice", 8, 2, 16);
        sellToPlayer(offers, currency, "minecraft:sweet_berries", 8, 2, 14);
        sellToPlayer(offers, currency, "minecraft:glow_berries", 4, 3, 10);
        sellToPlayer(offers, currency, "minecraft:beetroot_seeds", 8, 1, 20);
        sellToPlayer(offers, currency, "minecraft:pumpkin_seeds", 4, 2, 16);
        sellToPlayer(offers, currency, "minecraft:melon_seeds", 4, 2, 16);
        sellToPlayer(offers, currency, "minecraft:oak_sapling", 2, 3, 8);
        buyFromPlayer(offers, currency, "worldsmith:tomato", 12, 3, 12);
        buyFromPlayer(offers, currency, "worldsmith:lettuce", 10, 2, 12);
        buyFromPlayer(offers, currency, "worldsmith:cucumber", 12, 3, 12);
        buyFromPlayer(offers, currency, "worldsmith:strawberry", 12, 3, 12);
        buyFromPlayer(offers, currency, "worldsmith:blueberry", 16, 3, 12);
        buyFromPlayer(offers, currency, "worldsmith:raspberry", 12, 3, 12);
        buyFromPlayer(offers, currency, "worldsmith:pear", 8, 4, 10);
        buyFromPlayer(offers, currency, "worldsmith:peach", 8, 4, 10);
        buyFromPlayer(offers, currency, "worldsmith:orange", 8, 4, 10);
        buyFromPlayer(offers, currency, "minecraft:carrot", 16, 2, 12);
        buyFromPlayer(offers, currency, "minecraft:potato", 16, 2, 12);
        buyFromPlayer(offers, currency, "minecraft:beetroot", 16, 3, 10);
        buyFromPlayer(offers, currency, "minecraft:pumpkin", 4, 3, 8);
        buyFromPlayer(offers, currency, "minecraft:apple", 8, 3, 10);
        buyFromPlayer(offers, currency, "minecraft:sweet_berries", 16, 3, 10);
        buyFromPlayer(offers, currency, "minecraft:glow_berries", 8, 3, 8);
    }

    private static void sellToPlayer(
            MerchantOffers offers,
            Item currency,
            String itemId,
            int itemCount,
            int currencyCount,
            int maxUses
    ) {
        Item item = resolveItem(itemId);
        if (item == Items.AIR) {
            return;
        }
        offers.add(new MerchantOffer(
                new ItemStack(currency, currencyCount),
                new ItemStack(item, itemCount),
                maxUses,
                0,
                0.0F
        ));
    }

    private static void buyFromPlayer(
            MerchantOffers offers,
            Item currency,
            String itemId,
            int itemCount,
            int currencyCount,
            int maxUses
    ) {
        Item item = resolveItem(itemId);
        if (item == Items.AIR) {
            return;
        }
        offers.add(new MerchantOffer(
                new ItemStack(item, itemCount),
                new ItemStack(currency, currencyCount),
                maxUses,
                0,
                0.0F
        ));
    }

    private static Item resolveItem(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? Items.AIR : item;
    }
}
