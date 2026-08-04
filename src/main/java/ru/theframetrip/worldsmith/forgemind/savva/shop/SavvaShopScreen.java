package ru.theframetrip.worldsmith.forgemind.savva.shop;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class SavvaShopScreen extends AbstractContainerScreen<SavvaShopMenu> {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_WIDTH = 184;
    private static final int LIST_X = 12;
    private static final int LIST_Y = 38;
    private static final int PANEL_X = 204;
    private static final int PANEL_WIDTH = SavvaShopCatalog.SCREEN_WIDTH - PANEL_X - 12;

    private SavvaShopCatalog.Direction direction =
            SavvaShopCatalog.Direction.SELL_TO_PLAYER;
    private int selectedOffer = -1;
    private int scrollOffset;
    private int quantity = 1;
    private Button buyTab;
    private Button sellTab;
    private Button minusButton;
    private Button plusButton;
    private Button tradeButton;

    public SavvaShopScreen(
            SavvaShopMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = SavvaShopCatalog.SCREEN_WIDTH;
        imageHeight = SavvaShopCatalog.SCREEN_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        buyTab = addRenderableWidget(Button.builder(
                Component.literal(SavvaShopCatalog.BUY_LABEL),
                button -> setDirection(SavvaShopCatalog.Direction.SELL_TO_PLAYER)
        ).bounds(leftPos + 12, topPos + 12, 88, 20).build());
        sellTab = addRenderableWidget(Button.builder(
                Component.literal(SavvaShopCatalog.SELL_LABEL),
                button -> setDirection(SavvaShopCatalog.Direction.BUY_FROM_PLAYER)
        ).bounds(leftPos + 104, topPos + 12, 88, 20).build());
        minusButton = addRenderableWidget(Button.builder(
                Component.literal("−"),
                button -> {
                    quantity = Math.max(1, quantity - 1);
                    updateButtons();
                }
        ).bounds(leftPos + PANEL_X, topPos + imageHeight - 52, 28, 20).build());
        plusButton = addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> {
                    quantity = Math.min(maxQuantity(), quantity + 1);
                    updateButtons();
                }
        ).bounds(leftPos + PANEL_X + PANEL_WIDTH - 28, topPos + imageHeight - 52, 28, 20).build());
        tradeButton = addRenderableWidget(Button.builder(
                Component.literal(SavvaShopCatalog.BUY_LABEL),
                button -> submitTrade()
        ).bounds(leftPos + PANEL_X, topPos + imageHeight - 28, PANEL_WIDTH, 20).build());
        ensureSelection();
        updateButtons();
    }

    private void setDirection(SavvaShopCatalog.Direction nextDirection) {
        direction = nextDirection;
        scrollOffset = 0;
        selectedOffer = -1;
        quantity = 1;
        ensureSelection();
        updateButtons();
    }

    private void ensureSelection() {
        List<Integer> indexes = filteredOfferIndexes();
        if (!indexes.contains(selectedOffer)) {
            selectedOffer = indexes.isEmpty() ? -1 : indexes.get(0);
        }
    }

    private List<Integer> filteredOfferIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < SavvaShopCatalog.OFFERS.size(); index++) {
            if (SavvaShopCatalog.OFFERS.get(index).direction() == direction) {
                indexes.add(index);
            }
        }
        return indexes;
    }

    private void submitTrade() {
        if (selectedOffer < 0 || minecraft == null || minecraft.gameMode == null) {
            return;
        }
        int safeQuantity = Math.max(1, Math.min(quantity, maxQuantity()));
        if (safeQuantity < 1) {
            return;
        }
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId,
                menu.encodeTradeButton(selectedOffer, safeQuantity)
        );
    }

    private int maxQuantity() {
        if (selectedOffer < 0 || minecraft == null || minecraft.player == null) {
            return 0;
        }
        SavvaShopCatalog.Offer offer = SavvaShopCatalog.OFFERS.get(selectedOffer);
        int available;
        if (offer.direction() == SavvaShopCatalog.Direction.SELL_TO_PLAYER) {
            available = menu.getCurrencyCount() / offer.currencyCount();
        } else {
            available = minecraft.player.getInventory().countItem(offer.item())
                    / offer.itemCount();
        }
        return Math.max(
                0,
                Math.min(64, Math.min(menu.getRemainingUses(selectedOffer), available))
        );
    }

    private void updateButtons() {
        if (buyTab == null) {
            return;
        }
        buyTab.active = direction != SavvaShopCatalog.Direction.SELL_TO_PLAYER;
        sellTab.active = direction != SavvaShopCatalog.Direction.BUY_FROM_PLAYER;
        int maximum = maxQuantity();
        if (maximum == 0) {
            quantity = 1;
        } else {
            quantity = Math.max(1, Math.min(quantity, maximum));
        }
        minusButton.active = maximum > 0 && quantity > 1;
        plusButton.active = maximum > 0 && quantity < maximum;
        tradeButton.active = selectedOffer >= 0 && maximum > 0;
        String action = direction == SavvaShopCatalog.Direction.SELL_TO_PLAYER
                ? SavvaShopCatalog.BUY_LABEL
                : SavvaShopCatalog.SELL_LABEL;
        tradeButton.setMessage(Component.literal(action + " ×" + quantity));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF17130F);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFF33281B);
        graphics.fill(
                x + LIST_X - 4,
                y + LIST_Y - 4,
                x + LIST_X + LIST_WIDTH,
                y + LIST_Y + SavvaShopCatalog.VISIBLE_ROWS * ROW_HEIGHT + 4,
                0xFF211A13
        );
        graphics.fill(
                x + PANEL_X - 4,
                y + LIST_Y - 4,
                x + PANEL_X + PANEL_WIDTH,
                y + imageHeight - 60,
                0xFF211A13
        );
        graphics.drawString(font, SavvaShopCatalog.TITLE, x + PANEL_X, y + 14, 0xFFF4D9A6, false);
        ItemStack currency = new ItemStack(SavvaShopCatalog.currency());
        graphics.renderItem(currency, x + imageWidth - 62, y + 12);
        graphics.drawString(
                font,
                Integer.toString(menu.getCurrencyCount()),
                x + imageWidth - 42,
                y + 16,
                0xFFFFE3A0,
                false
        );
        renderOfferRows(graphics, mouseX, mouseY);
        renderSelectedOffer(graphics);
    }

    private void renderOfferRows(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Integer> indexes = filteredOfferIndexes();
        int maxScroll = Math.max(0, indexes.size() - SavvaShopCatalog.VISIBLE_ROWS);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        for (int row = 0; row < SavvaShopCatalog.VISIBLE_ROWS; row++) {
            int filteredIndex = scrollOffset + row;
            if (filteredIndex >= indexes.size()) {
                break;
            }
            int offerIndex = indexes.get(filteredIndex);
            SavvaShopCatalog.Offer offer = SavvaShopCatalog.OFFERS.get(offerIndex);
            int rowX = leftPos + LIST_X;
            int rowY = topPos + LIST_Y + row * ROW_HEIGHT;
            boolean hovered = mouseX >= rowX && mouseX < rowX + LIST_WIDTH - 4
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2;
            int background = offerIndex == selectedOffer
                    ? 0xFF6C4B28
                    : hovered ? 0xFF4C3924 : 0xFF2E2419;
            graphics.fill(rowX, rowY, rowX + LIST_WIDTH - 4, rowY + ROW_HEIGHT - 2, background);
            ItemStack stack = new ItemStack(offer.item(), offer.itemCount());
            graphics.renderItem(stack, rowX + 3, rowY + 2);
            graphics.renderItemDecorations(font, stack, rowX + 3, rowY + 2);
            String name = font.plainSubstrByWidth(stack.getHoverName().getString(), 100);
            graphics.drawString(font, name, rowX + 24, rowY + 4, 0xFFF3E6C9, false);
            graphics.drawString(
                    font,
                    offer.currencyCount() + " м.",
                    rowX + 24,
                    rowY + 13,
                    0xFFE5BD75,
                    false
            );
            graphics.drawString(
                    font,
                    "×" + menu.getRemainingUses(offerIndex),
                    rowX + LIST_WIDTH - 34,
                    rowY + 8,
                    0xFFC9B58D,
                    false
            );
        }
    }

    private void renderSelectedOffer(GuiGraphics graphics) {
        if (selectedOffer < 0) {
            return;
        }
        SavvaShopCatalog.Offer offer = SavvaShopCatalog.OFFERS.get(selectedOffer);
        int x = leftPos + PANEL_X;
        int y = topPos + LIST_Y;
        ItemStack stack = new ItemStack(offer.item(), offer.itemCount());
        graphics.renderItem(stack, x + 4, y + 8);
        graphics.renderItemDecorations(font, stack, x + 4, y + 8);
        graphics.drawString(
                font,
                font.plainSubstrByWidth(stack.getHoverName().getString(), PANEL_WIDTH - 28),
                x + 26,
                y + 10,
                0xFFF4D9A6,
                false
        );
        graphics.drawString(font, "Количество: " + offer.itemCount(), x + 4, y + 38, 0xFFD8C8A8, false);
        graphics.drawString(font, "Цена: " + offer.currencyCount(), x + 4, y + 51, 0xFFE5BD75, false);
        graphics.drawString(
                font,
                "Осталось: " + menu.getRemainingUses(selectedOffer),
                x + 4,
                y + 64,
                0xFFD8C8A8,
                false
        );
        graphics.drawString(font, "Выбрано: " + quantity, x + 4, y + 90, 0xFFF3E6C9, false);
        graphics.drawString(
                font,
                "Итого: " + offer.currencyCount() * quantity,
                x + 4,
                y + 103,
                0xFFFFD17A,
                false
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int localX = (int) mouseX - leftPos;
            int localY = (int) mouseY - topPos;
            if (localX >= LIST_X && localX < LIST_X + LIST_WIDTH - 4
                    && localY >= LIST_Y
                    && localY < LIST_Y + SavvaShopCatalog.VISIBLE_ROWS * ROW_HEIGHT) {
                int row = (localY - LIST_Y) / ROW_HEIGHT;
                List<Integer> indexes = filteredOfferIndexes();
                int filteredIndex = scrollOffset + row;
                if (filteredIndex >= 0 && filteredIndex < indexes.size()) {
                    selectedOffer = indexes.get(filteredIndex);
                    quantity = 1;
                    updateButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<Integer> indexes = filteredOfferIndexes();
        int maxScroll = Math.max(0, indexes.size() - SavvaShopCatalog.VISIBLE_ROWS);
        if (delta > 0.0D) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (delta < 0.0D) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        return true;
    }
}
