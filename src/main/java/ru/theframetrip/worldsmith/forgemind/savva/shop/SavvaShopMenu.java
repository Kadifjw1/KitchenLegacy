package ru.theframetrip.worldsmith.forgemind.savva.shop;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class SavvaShopMenu extends AbstractContainerMenu {
    private static final int INVENTORY_SIZE = 36;
    private static final int MAX_QUANTITY = 64;
    private static final int DATA_COUNT = 1 + SavvaShopCatalog.OFFERS.size();
    private static final int QUANTITY_BITS = 6;
    private static final int QUANTITY_MASK = (1 << QUANTITY_BITS) - 1;

    private final Inventory playerInventory;
    private final Villager savva;
    private final int savvaEntityId;
    private final ContainerData data;

    public SavvaShopMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                null,
                buffer.readVarInt(),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public SavvaShopMenu(int containerId, Inventory playerInventory, Villager savva) {
        this(
                containerId,
                playerInventory,
                savva,
                savva.getId(),
                createServerData(playerInventory, savva)
        );
    }

    private SavvaShopMenu(
            int containerId,
            Inventory playerInventory,
            Villager savva,
            int savvaEntityId,
            ContainerData data
    ) {
        super(SavvaShopRegistry.SAVVA_SHOP.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.playerInventory = playerInventory;
        this.savva = savva;
        this.savvaEntityId = savvaEntityId;
        this.data = data;
        addDataSlots(data);
    }

    private static ContainerData createServerData(Inventory inventory, Villager savva) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return countItem(inventory, SavvaShopCatalog.currency());
                }
                int offerIndex = index - 1;
                if (offerIndex < 0 || offerIndex >= savva.getOffers().size()) {
                    return 0;
                }
                MerchantOffer offer = savva.getOffers().get(offerIndex);
                return Math.max(0, offer.getMaxUses() - offer.getUses());
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public int getSavvaEntityId() {
        return savvaEntityId;
    }

    public int getCurrencyCount() {
        return data.get(0);
    }

    public int getRemainingUses(int offerIndex) {
        if (offerIndex < 0 || offerIndex >= SavvaShopCatalog.OFFERS.size()) {
            return 0;
        }
        return data.get(offerIndex + 1);
    }

    public int encodeTradeButton(int offerIndex, int quantity) {
        int safeQuantity = Math.max(1, Math.min(MAX_QUANTITY, quantity));
        return (offerIndex << QUANTITY_BITS) | (safeQuantity - 1);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer) || savva == null) {
            return false;
        }
        int offerIndex = buttonId >> QUANTITY_BITS;
        int quantity = (buttonId & QUANTITY_MASK) + 1;
        if (offerIndex < 0 || offerIndex >= SavvaShopCatalog.OFFERS.size()) {
            return false;
        }
        if (!stillValid(player) || !SavvaShopCatalog.isOpen(player.level().getDayTime())) {
            return false;
        }
        SavvaCustomShopFeature.refreshDailyStock(savva, player.level().getDayTime());
        boolean traded = executeTrade(serverPlayer, offerIndex, quantity);
        if (traded) {
            broadcastChanges();
        }
        return traded;
    }

    private boolean executeTrade(ServerPlayer player, int offerIndex, int quantity) {
        MerchantOffer merchantOffer = savva.getOffers().get(offerIndex);
        SavvaShopCatalog.Offer definition = SavvaShopCatalog.OFFERS.get(offerIndex);
        int remaining = merchantOffer.getMaxUses() - merchantOffer.getUses();
        if (quantity < 1 || quantity > MAX_QUANTITY || quantity > remaining) {
            player.displayClientMessage(ComponentText.stockChanged(), true);
            return false;
        }
        if (!matchesDefinition(merchantOffer, definition)) {
            player.displayClientMessage(ComponentText.profileChanged(), true);
            return false;
        }

        Item item = definition.item();
        Item currency = SavvaShopCatalog.currency();
        Inventory inventory = player.getInventory();
        int itemTotal = Math.multiplyExact(definition.itemCount(), quantity);
        int currencyTotal = Math.multiplyExact(definition.currencyCount(), quantity);

        if (definition.direction() == SavvaShopCatalog.Direction.SELL_TO_PLAYER) {
            if (countItem(inventory, currency) < currencyTotal) {
                player.displayClientMessage(ComponentText.notEnoughCurrency(), true);
                return false;
            }
            if (!canFit(inventory, new ItemStack(item), itemTotal)) {
                player.displayClientMessage(ComponentText.noSpace(), true);
                return false;
            }
            removeItem(inventory, currency, currencyTotal);
            giveItem(inventory, item, itemTotal);
        } else {
            if (countItem(inventory, item) < itemTotal) {
                player.displayClientMessage(ComponentText.notEnoughItems(), true);
                return false;
            }
            if (!canFit(inventory, new ItemStack(currency), currencyTotal)) {
                player.displayClientMessage(ComponentText.noSpace(), true);
                return false;
            }
            removeItem(inventory, item, itemTotal);
            giveItem(inventory, currency, currencyTotal);
        }

        for (int index = 0; index < quantity; index++) {
            merchantOffer.increaseUses();
        }
        player.getInventory().setChanged();
        return true;
    }

    private static boolean matchesDefinition(
            MerchantOffer offer,
            SavvaShopCatalog.Offer definition
    ) {
        Item currency = SavvaShopCatalog.currency();
        Item item = definition.item();
        if (definition.direction() == SavvaShopCatalog.Direction.SELL_TO_PLAYER) {
            return offer.getCostA().is(currency)
                    && offer.getCostA().getCount() == definition.currencyCount()
                    && offer.getResult().is(item)
                    && offer.getResult().getCount() == definition.itemCount();
        }
        return offer.getCostA().is(item)
                && offer.getCostA().getCount() == definition.itemCount()
                && offer.getResult().is(currency)
                && offer.getResult().getCount() == definition.currencyCount();
    }

    private static int countItem(Inventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < Math.min(INVENTORY_SIZE, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void removeItem(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < Math.min(INVENTORY_SIZE, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (remaining == 0) {
                return;
            }
        }
        throw new IllegalStateException("Validated inventory changed during Savva trade");
    }

    private static boolean canFit(Inventory inventory, ItemStack template, int amount) {
        int capacity = 0;
        for (int slot = 0; slot < Math.min(INVENTORY_SIZE, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                capacity += template.getMaxStackSize();
            } else if (ItemStack.isSameItemSameTags(stack, template)) {
                capacity += Math.max(0, stack.getMaxStackSize() - stack.getCount());
            }
            if (capacity >= amount) {
                return true;
            }
        }
        return false;
    }

    private static void giveItem(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        int maxStackSize = new ItemStack(item).getMaxStackSize();
        while (remaining > 0) {
            int count = Math.min(maxStackSize, remaining);
            ItemStack stack = new ItemStack(item, count);
            if (!inventory.add(stack) || !stack.isEmpty()) {
                throw new IllegalStateException("Validated inventory rejected Savva trade result");
            }
            remaining -= count;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (savva == null) {
            return true;
        }
        return savva.isAlive()
                && SavvaShopCatalog.ROLE_ID.equals(
                        savva.getPersistentData().getString(SavvaShopCatalog.ROLE_TAG)
                )
                && player.distanceToSqr(savva) <= 64.0D;
    }

    private static final class ComponentText {
        private ComponentText() {
        }

        private static net.minecraft.network.chat.Component stockChanged() {
            return net.minecraft.network.chat.Component.literal("Запас изменился. Выберите количество заново.");
        }

        private static net.minecraft.network.chat.Component profileChanged() {
            return net.minecraft.network.chat.Component.literal("Профиль лавки изменился. Откройте окно заново.");
        }

        private static net.minecraft.network.chat.Component notEnoughCurrency() {
            return net.minecraft.network.chat.Component.literal("Недостаточно монет.");
        }

        private static net.minecraft.network.chat.Component notEnoughItems() {
            return net.minecraft.network.chat.Component.literal("Недостаточно товара для продажи.");
        }

        private static net.minecraft.network.chat.Component noSpace() {
            return net.minecraft.network.chat.Component.literal("В инвентаре недостаточно места.");
        }
    }
}
