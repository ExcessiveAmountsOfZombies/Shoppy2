package com.epherical.shoppy.client.widget;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.network.payloads.PurchaseAttemptPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/**
 * One clickable row inside the bartering GUI.
 * When the user clicks it a {@link PurchaseAttemptPayload} is sent.
 */
public class RowButton extends AbstractWidget {

    /* ---- layout constants (copy of BarteringScreen’s values) ---- */
    private static final int ITEM_SIZE   = 16;
    private static final int TEXT_Y_OFF  = 6;
    private static final int ROW_PAD_X   = 8;

    private final BarteringBlockEntity bartering;
    private final int offerIndex;

    public RowButton(int x, int y, int width, int height,
                     int offerIndex, BarteringBlockEntity bartering) {
        super(x, y, width, height, Component.empty());
        this.offerIndex = offerIndex;
        this.bartering = bartering;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovering = this.isMouseOver(mouseX, mouseY);

        int colour = hovering ? 0xAAFFFFFF : 0x88000000;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, colour);

        ItemStack saleItem = bartering.getSaleItem();
        int saleCnt = bartering.getSaleCount(offerIndex);
        if (!saleItem.isEmpty()) {
            graphics.renderItem(saleItem, getX() + 4, getY() + 1);
            graphics.drawString(Minecraft.getInstance().font, String.valueOf(saleCnt),
                    getX() + 4 + ITEM_SIZE + 4, getY() + TEXT_Y_OFF, 0xFFFFFF, false);

            if (isHovering(mouseX, mouseY, getX() + 4, getY() + 1)) {
                showTooltip(graphics, mouseX, mouseY, saleItem,
                        Component.translatable("tooltip.shoppy.sale_item"));
            }
        }

        ItemStack curItem = bartering.getCurrencyItem();
        int costCnt = bartering.getCostCount(offerIndex);
        if (!curItem.isEmpty()) {
            int curX = getX() + width - ITEM_SIZE - 40;
            graphics.renderItem(curItem, curX, getY() + 1);
            graphics.drawString(Minecraft.getInstance().font, String.valueOf(costCnt),
                    curX + ITEM_SIZE + 4, getY() + TEXT_Y_OFF, 0xFFFFFF, false);

            if (isHovering(mouseX, mouseY, curX, getY() + 1)) {
                showTooltip(graphics, mouseX, mouseY, curItem,
                        Component.translatable("tooltip.shoppy.currency_item"));
            }
        }
    }

    private static boolean isHovering(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + ITEM_SIZE
            && mouseY >= y && mouseY < y + ITEM_SIZE;
    }

    private static void showTooltip(GuiGraphics g, int mouseX, int mouseY,
                                    ItemStack stack, Component extraLine) {
        Minecraft mc = Minecraft.getInstance();
        List<Component> lines = stack.getTooltipLines(
                Item.TooltipContext.of(mc.player.level()),
                mc.player,
                mc.options.advancedItemTooltips
                        ? TooltipFlag.Default.ADVANCED
                        : TooltipFlag.Default.NORMAL);
        lines.add(extraLine);
        g.renderTooltip(mc.font, lines, Optional.empty(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        CustomPacketPayload payload = new PurchaseAttemptPayload(offerIndex);
        PacketDistributor.sendToServer(payload);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
