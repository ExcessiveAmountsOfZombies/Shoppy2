package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.client.widget.AddItemButton;
import com.epherical.shoppy.client.widget.ShopItemWidget;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.payloads.AddItemRequestPayload;
import com.epherical.shoppy.network.payloads.PriceSubmissionPayload;
import com.epherical.shoppy.network.payloads.StockTransferPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Optional;

import static com.epherical.shoppy.Shoppy.MODID;

public class BarteringScreenOwner extends AbstractContainerScreen<BarteringMenuOwner> {

    private static final int ITEM_SIZE = 16;


    private static final int SALE_ITEM_X = 26;   // pixels from leftPos
    private static final int SALE_ITEM_Y = 48;   // pixels from topPos
    private static final int CURRENCY_ITEM_X = 111;
    private static final int CURRENCY_ITEM_Y = 133;

    private static final int ROW_START_Y = 30;   // first row offset from topPos
    private static final int ROW_HEIGHT = 20;   // distance between rows
    private static final int ROW_PAD_X = 8;    // left / right padding inside BG
    private static final int TEXT_Y_OFF = 6;    // text offset inside a row

    private EditBox priceField;
    private EditBox receivedField;


    private BarteringBlockEntity bartering;

    public static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/bartering_page_owner.png");


    private static final Component ADD_ITEM_BTN = Component.translatable("screen.shoppy.add_price");


    public BarteringScreenOwner(BarteringMenuOwner pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = -3000;
    }

    @Override
    protected void init() {
        super.init();


        bartering = (BarteringBlockEntity) minecraft.level.getBlockEntity(menu.getBlockPos());

        int rows = populatedRows();
        int btnX = leftPos + (imageWidth - 150) / 2;
        int btnY = topPos + ROW_START_Y + rows * ROW_HEIGHT;

        if (rows < 3 && !getMenu().isEditing()) {
            addRenderableWidget(
                    AddItemButton.addItem(ADD_ITEM_BTN,
                                    b -> ClientPacketDistributor.sendToServer(new AddItemRequestPayload(menu.getBlockPos())))
                            .size(68, 14).pos(btnX, btnY).build());
        }


        if (getMenu().isEditing()) {

            priceField = this.addRenderableWidget(new EditBox(minecraft.font, btnX + 24, btnY + 2, 30, 14, Component.literal("Price")));
            priceField.setTooltip(Tooltip.create(Component.literal("Price -- The price that the player must pay to 'receive' any items.")));
            receivedField = this.addRenderableWidget(new EditBox(minecraft.font, btnX - 8, btnY + 2, 30, 14, Component.literal("Received")));
            receivedField.setTooltip(Tooltip.create(Component.literal("Received -- What the player will get in return for giving you the price of the item.")));
            Checkbox checkbox1 = this.addRenderableWidget(Checkbox.builder(Component.literal(""), minecraft.font)
                    .selected(true)
                    .pos(btnX + 30 + 25, btnY + 1)
                    .onValueChange((checkbox, value) -> submitOffer()).build());
            checkbox1.setTooltip(Tooltip.create(Component.literal("Submit the pricing.")));
        }

        btnX = leftPos + (imageWidth - 150) / 2;
        btnY = topPos + 25;

        addRenderableWidget(Button.builder(Component.translatable("screen.shoppy.set_item"), button -> {
            minecraft.setScreen(new ShopPickingCreativeInventoryScreen(minecraft.player, this.minecraft.player.connection.enabledFeatures(), false, true));
        }).size(60, 14).pos(btnX + 98, btnY + 147 - 80).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.shoppy.set_sold"), button -> {
            minecraft.setScreen(new ShopPickingCreativeInventoryScreen(minecraft.player, this.minecraft.player.connection.enabledFeatures(), false, false));
        }).size(60, 14).pos(btnX + 98, btnY + 147 - 14 - 80).build());

        ShopItemWidget saleIcon = new ShopItemWidget(
                leftPos + CURRENCY_ITEM_X, topPos + CURRENCY_ITEM_Y - ITEM_SIZE - 72,
                bartering::getSaleItem,
                () -> menu.getContainerData().get(0),
                btn -> {
                    boolean insert = btn == 0;   // 0 = LMB ➜ insert, 1 = RMB ➜ extract
                    ClientPacketDistributor.sendToServer(
                            new StockTransferPayload(bartering.getBlockPos(), true, insert));

                }, Component.translatable("tooltip.shoppy.sale_item")
                .withStyle(ChatFormatting.GRAY));
        addRenderableWidget(saleIcon);


        ShopItemWidget currencyIcon = new ShopItemWidget(
                leftPos + CURRENCY_ITEM_X,
                topPos + CURRENCY_ITEM_Y - 72,
                bartering::getCurrencyItem,
                () -> menu.getContainerData().get(1),
                button -> ClientPacketDistributor.sendToServer(
                        new StockTransferPayload(bartering.getBlockPos(), false, false)),
                Component.translatable("tooltip.shoppy.currency_item")
                        .withStyle(ChatFormatting.GRAY)   // extra tooltip line
        );

        addRenderableWidget(currencyIcon);


    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        int left = leftPos;
        int top = topPos;
        this.renderTooltip(graphics, pMouseX, pMouseY);

        if (bartering != null) {
            /*ItemStack saleStack = bartering.getSaleItem();
            if (!saleStack.isEmpty()) {
                int x = left + CURRENCY_ITEM_X;
                int y = top + CURRENCY_ITEM_Y - ITEM_SIZE - 72;

                if (isHovering(pMouseX, pMouseY, x, y)) {
                    List<Component> lines = saleStack
                            .getTooltipLines(
                                    Item.TooltipContext.of(minecraft.player.level()),
                                    minecraft.player,
                                    minecraft.options.advancedItemTooltips
                                            ? TooltipFlag.Default.ADVANCED
                                            : TooltipFlag.Default.NORMAL);

                    lines.add(Component.translatable("tooltip.shoppy.sale_item")
                            .withStyle(ChatFormatting.GRAY));
                    graphics.renderTooltip(this.font, lines, Optional.empty(), pMouseX, pMouseY);
                }
                graphics.renderItem(saleStack, x, y);
                graphics.drawString(this.font, "x" + menu.getContainerData().get(0), x + 16, y + 4, 0xFFFFFF);
                graphics.renderItemDecorations(this.font, saleStack, x, y);
            }

            ItemStack currencyStack = bartering.getCurrencyItem();
            if (!currencyStack.isEmpty()) {
                int x = left + CURRENCY_ITEM_X;
                int y = top + CURRENCY_ITEM_Y - 72;

                if (isHovering(pMouseX, pMouseY, x, y)) {
                    List<Component> lines = bartering.getCurrencyItem()
                            .getTooltipLines(
                                    Item.TooltipContext.of(minecraft.player.level()),
                                    minecraft.player,
                                    minecraft.options.advancedItemTooltips
                                            ? TooltipFlag.Default.ADVANCED
                                            : TooltipFlag.Default.NORMAL);

                    lines.add(Component.translatable("tooltip.shoppy.currency_item")
                            .withStyle(ChatFormatting.GRAY));
                    graphics.renderTooltip(this.font, lines, Optional.empty(), pMouseX, pMouseY);
                }

                graphics.renderItem(currencyStack, x, y);
                graphics.drawString(this.font, "x" + menu.getContainerData().get(1), x + 16, y + 4, 0xFFFFFF);
                graphics.renderItemDecorations(this.font, currencyStack, x, y);
            }*/
        }

        for (int i = 0; i < 3; i++) {
            int saleCnt = bartering.getSaleCount(i);
            int costCnt = bartering.getCostCount(i);
            if (saleCnt == 0 && costCnt == 0) continue;

            int rowY = topPos + ROW_START_Y + i * ROW_HEIGHT;

            left = leftPos + ROW_PAD_X - 2;
            int right = leftPos + 110 - ROW_PAD_X;
            graphics.fill(left, rowY, right, rowY + ROW_HEIGHT - 2, 0x88000000);

            ItemStack saleItem = bartering.getSaleItem();
            if (!saleItem.isEmpty()) {
                graphics.renderItem(saleItem, left + 4, rowY + 1);
                graphics.drawString(minecraft.font, String.valueOf(saleCnt),
                        left + 4 + ITEM_SIZE + 4, rowY + TEXT_Y_OFF, 0xFFFFFFFF, false);


                if (isHovering(pMouseX, pMouseY, left + 4, rowY + 1)) {
                    List<Component> lines = bartering.getSaleItem()
                            .getTooltipLines(
                                    Item.TooltipContext.of(minecraft.player.level()),
                                    minecraft.player,
                                    minecraft.options.advancedItemTooltips
                                            ? TooltipFlag.Default.ADVANCED
                                            : TooltipFlag.Default.NORMAL);

                    lines.add(Component.translatable("tooltip.shoppy.sale_item")
                            .withStyle(ChatFormatting.GRAY));
                    graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), pMouseX, pMouseY);
                }

            }

            ItemStack curItem = bartering.getCurrencyItem();
            if (!curItem.isEmpty()) {
                int curX = right - ITEM_SIZE - 40;
                graphics.renderItem(curItem, curX, rowY + 1);
                graphics.drawString(minecraft.font, String.valueOf(costCnt),
                        curX + ITEM_SIZE + 4, rowY + TEXT_Y_OFF, 0xFFFFFFFF, false);

                if (isHovering(pMouseX, pMouseY, curX, rowY + 1)) {
                    List<Component> lines = bartering.getCurrencyItem()
                            .getTooltipLines(
                                    Item.TooltipContext.of(minecraft.player.level()),
                                    minecraft.player,
                                    minecraft.options.advancedItemTooltips
                                            ? TooltipFlag.Default.ADVANCED
                                            : TooltipFlag.Default.NORMAL);

                    lines.add(Component.translatable("tooltip.shoppy.currency_item")
                            .withStyle(ChatFormatting.GRAY));
                    graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), pMouseX, pMouseY);
                }
            }
        }
    }

    public static boolean isHovering(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + ITEM_SIZE
                && mouseY >= y && mouseY < y + ITEM_SIZE;
    }


    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, left, top, 0.0F, 0.0F, 176, 191, 256, 256);
    }


    private void submitOffer() {
        int price = safeParse(priceField.getValue());
        int received = safeParse(receivedField.getValue());

        var payload = new PriceSubmissionPayload(populatedRows(), price, received); // offerIndex 0; adjust as needed
        ClientPacketDistributor.sendToServer(payload);
    }

    private static int safeParse(String txt) {
        try {
            return Integer.parseInt(txt.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int populatedRows() {
        if (bartering == null) return 0;
        int rows = 0;
        for (int i = 0; i < 3; i++)
            if (bartering.getSaleCount(i) > 0 || bartering.getCostCount(i) > 0)
                rows++;
        return rows;
    }

}
