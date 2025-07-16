package com.epherical.shoppy.client.widget;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

/**
 * One clickable row inside the bartering GUI.
 *
 * Clicking once reveals a confirmation button; clicking that second button
 * executes the supplied action.  While waiting for confirmation the row keeps
 * the "hovered" colour.
 */
public class RowButton extends AbstractWidget {

    /* ---- layout constants (copy of BarteringScreen’s values) ---- */
    private static final int ITEM_SIZE   = 16;
    private static final int TEXT_Y_OFF  = 6;
    private static final int ROW_PAD_X   = 8;

    /* confirmation button */
    private static final int CONFIRM_W   = 52;
    private static final int CONFIRM_H   = 16;

    private final BarteringBlockEntity bartering;
    private final int offerIndex;
    private final IntConsumer confirmAction;

    /* runtime state ------------------------------------------------------ */
    private boolean awaitingConfirm = false;

    public RowButton(int x, int y, int width, int height,
                     int offerIndex,
                     BarteringBlockEntity bartering,
                     IntConsumer confirmAction) {

        super(x, y, width, height, Component.empty());
        this.offerIndex     = offerIndex;
        this.bartering      = bartering;
        this.confirmAction  = confirmAction;
    }

    /* ------------------------------------------------------------------ */
    /*  Rendering                                                         */
    /* ------------------------------------------------------------------ */

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovering = isMouseOver(mouseX, mouseY);
        int colour = (hovering || awaitingConfirm) ? 0xAAFFFFFF : 0x88000000;
        g.fill(getX(), getY(), getX() + width, getY() + height, colour);

        renderOffer(g, mouseX, mouseY);
        if (awaitingConfirm) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 1000F);
            renderConfirmButton(g, mouseX, mouseY);
            g.pose().translate(0, 0, -1000F);
            g.pose().popPose();
        }
    }

    private void renderOffer(GuiGraphics g, int mouseX, int mouseY) {
        ItemStack saleItem = bartering.getSaleItem();
        int saleCnt        = bartering.getSaleCount(offerIndex);
        if (!saleItem.isEmpty()) {
            g.renderItem(saleItem, getX() + ROW_PAD_X, getY() + 1);
            g.drawString(Minecraft.getInstance().font, String.valueOf(saleCnt),
                    getX() + ROW_PAD_X + ITEM_SIZE + 4, getY() + TEXT_Y_OFF, 0xFFFFFF, false);

            if (isHovering(mouseX, mouseY, getX() + ROW_PAD_X, getY() + 1)) {
                showTooltip(g, mouseX, mouseY, saleItem,
                        Component.translatable("tooltip.shoppy.sale_item"));
            }
        }

        ItemStack curItem   = bartering.getCurrencyItem();
        int costCnt         = bartering.getCostCount(offerIndex);
        if (!curItem.isEmpty()) {
            int curX = getX() + width - ITEM_SIZE - 40;
            g.renderItem(curItem, curX, getY() + 1);
            g.drawString(Minecraft.getInstance().font, String.valueOf(costCnt),
                    curX + ITEM_SIZE + 4, getY() + TEXT_Y_OFF, 0xFFFFFF, false);

            if (isHovering(mouseX, mouseY, curX, getY() + 1)) {
                showTooltip(g, mouseX, mouseY, curItem,
                        Component.translatable("tooltip.shoppy.currency_item"));
            }
        }
    }

    private void renderConfirmButton(GuiGraphics g, int mouseX, int mouseY) {
        int btnX = getX() + width - CONFIRM_W - ROW_PAD_X;
        int btnY = getY() + (height - CONFIRM_H) / 2;

        boolean hover = mouseX >= btnX && mouseX < btnX + CONFIRM_W
                     && mouseY >= btnY && mouseY < btnY + CONFIRM_H;

        int bg = hover ? 0xFF4CAF50 : 0xFF388E3C; // simple green shades
        g.fill(btnX, btnY, btnX + CONFIRM_W, btnY + CONFIRM_H, bg);

        Component txt = Component.translatable("screen.shoppy.confirm");
        int txtW = Minecraft.getInstance().font.width(txt);
        g.drawString(Minecraft.getInstance().font, txt,
                btnX + (CONFIRM_W - txtW) / 2,
                btnY + 4, 0xFFFFFF, false);
    }

    /* ------------------------------------------------------------------ */
    /*  Interaction                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (awaitingConfirm) {
            if (insideConfirm(mouseX, mouseY)) {
                confirmAction.accept(offerIndex);        // submit
                awaitingConfirm = false;                 // reset visual state
            }
        } else {
            awaitingConfirm = true;                      // first click -> show button
        }
    }

    private boolean insideConfirm(double mouseX, double mouseY) {
        int btnX = getX() + width - CONFIRM_W - ROW_PAD_X;
        int btnY = getY() + (height - CONFIRM_H) / 2;
        return mouseX >= btnX && mouseX < btnX + CONFIRM_W
            && mouseY >= btnY && mouseY < btnY + CONFIRM_H;
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                           */
    /* ------------------------------------------------------------------ */

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
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        // left empty – not required for now
    }
}
