package ru.theframetrip.worldsmith.forgemind.savva.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import ru.theframetrip.worldsmith.WorldsmithMod;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SavvaCustomShopFeature {
    private SavvaCustomShopFeature() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getTarget() instanceof Villager savva)) {
            return;
        }
        if (!SavvaShopCatalog.ROLE_ID.equals(
                savva.getPersistentData().getString(SavvaShopCatalog.ROLE_TAG)
        )) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!SavvaShopCatalog.isOpen(player.serverLevel().getDayTime())) {
            player.displayClientMessage(
                    Component.literal("На сегодня лавка закрыта. Урожаю тоже нужен отдых."),
                    true
            );
            return;
        }
        if (savva.getOffers().size() != SavvaShopCatalog.OFFERS.size()) {
            player.displayClientMessage(
                    Component.literal("Лавка Саввы ещё не готова. Создайте продавца заново."),
                    true
            );
            return;
        }

        refreshDailyStock(savva, player.serverLevel().getDayTime());
        NetworkHooks.openScreen(
                player,
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new SavvaShopMenu(containerId, inventory, savva),
                        Component.literal(SavvaShopCatalog.TITLE)
                ),
                buffer -> buffer.writeVarInt(savva.getId())
        );
    }

    static void refreshDailyStock(Villager savva, long dayTime) {
        long day = dayTime / 24000L;
        long previous = savva.getPersistentData().getLong(
                SavvaShopCatalog.LAST_RESTOCK_DAY_TAG
        );
        if (day == previous) {
            return;
        }
        for (MerchantOffer offer : savva.getOffers()) {
            offer.resetUses();
        }
        savva.getPersistentData().putLong(
                SavvaShopCatalog.LAST_RESTOCK_DAY_TAG,
                day
        );
    }
}
