package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.client.widget.RowButton;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.network.payloads.PurchaseAttemptPayload;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

import static com.epherical.shoppy.Shoppy.MODID;

public class BarteringScreen extends AbstractContainerScreen<BarteringMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/bartering_page_owner.png");

    private static final int ROW_START_Y = 30;   // first row offset from topPos
    private static final int ROW_HEIGHT = 20;   // distance between rows
    private static final int ROW_PAD_X = 8;    // left / right padding inside BG
    private static final int TEXT_Y_OFF = 6;    // text offset inside a row
    private static final int ITEM_SIZE = 16;


    private static final int CURRENCY_ITEM_X  = 111;
    private static final int CURRENCY_ITEM_Y  = 133;

    private BarteringBlockEntity bartering;


    public BarteringScreen(BarteringMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = -3000;

    }


    @Override
    protected void init() {
        super.init();
        bartering = (BarteringBlockEntity) minecraft.level.getBlockEntity(menu.getBlockPos());
        for (int i = 0; i < 3; i++) {
            if (bartering.getSaleCount(i) == 0 && bartering.getCostCount(i) == 0) continue;  // nothing to show

            int y = topPos + ROW_START_Y + i * ROW_HEIGHT;
            int x = leftPos + ROW_PAD_X - 2;
            int width = 110 - ROW_PAD_X * 2 + 2;

            RowButton btn = new RowButton(x, y, width, ROW_HEIGHT - 2, i, bartering,
                    ii -> ClientPacketDistributor.sendToServer(new PurchaseAttemptPayload(ii)));
            this.addRenderableWidget(btn);

        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(graphics, pMouseX, pMouseY);
        int left = leftPos;
        int top = topPos;
        if (bartering != null) {
            ItemStack saleStack = bartering.getSaleItem();
            if (!saleStack.isEmpty()) {
                int x = left + CURRENCY_ITEM_X;
                int y = top  + CURRENCY_ITEM_Y - ITEM_SIZE - 112;

                if (BarteringScreenOwner.isHovering(pMouseX, pMouseY, x, y)) {
                    List<Component> lines = saleStack
                            .getTooltipLines(
                                    Item.TooltipContext.of(minecraft.player.level()),
                                    minecraft.player,
                                    minecraft.options.advancedItemTooltips
                                            ? TooltipFlag.Default.ADVANCED
                                            : TooltipFlag.Default.NORMAL);

                    lines.add(Component.translatable("tooltip.shoppy.sale_stock")
                            .withStyle(ChatFormatting.GRAY));
                    graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), pMouseX, pMouseY);
                }
                graphics.renderItem(saleStack, x, y);
                graphics.drawString(this.font, "x"+menu.getContainerData().get(0), x + 16, y + 4, 0xFFFFFFFF);
                graphics.renderItemDecorations(this.font, saleStack, x, y);
            }

            ItemStack currencyStack = bartering.getCurrencyItem();
            if (!currencyStack.isEmpty()) {
                int x = left + CURRENCY_ITEM_X;
                int y = top  + CURRENCY_ITEM_Y - 112;

                if (BarteringScreenOwner.isHovering(pMouseX, pMouseY, x, y)) {
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

                // guiGraphics.renderTooltip(
                //                font,
                //                List.of(clienttooltipcomponent),
                //                j - i / 2,
                //                y - 15,
                //                DefaultTooltipPositioner.INSTANCE,
                //                itemstack.get(DataComponents.TOOLTIP_STYLE)
                //            );

                graphics.renderItem(currencyStack, x, y);
                graphics.drawString(this.font, "x"+menu.getContainerData().get(1), x + 16, y + 4, 0xFFFFFFFF);
                graphics.renderItemDecorations(this.font, currencyStack, x, y);
            }
        }



    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, left, top, 0.0F, 0.0F, 176, 191, 256, 256);
    }
}
