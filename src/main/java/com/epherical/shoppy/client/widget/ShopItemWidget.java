package com.epherical.shoppy.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Simple 16×16 clickable item icon that shows a stack count and tooltip, and
 * reports left / right clicks through a small callback.
 * <p>
 * Construction parameters:
 * • xy – absolute position inside the parent screen
 * • stackSupplier  – returns the ItemStack to draw (can change at runtime)
 * • countSupplier  – returns the number to print next to the icon
 * • onClick(int mouseButton) – called with 0 = left, 1 = right whenever the
 * user clicks the icon while hovering it
 */
public class ShopItemWidget extends AbstractWidget {

    private static final int ITEM_SIZE = 16;
    private static final int TEXT_Y_OFF = 6;
    private static final int COUNT_PAD_X = ITEM_SIZE;

    private final Supplier<ItemStack> stackSupplier;
    private final IntSupplier countSupplier;
    private final IntConsumer onClick;

    private final MutableComponent contents;

    public ShopItemWidget(int x, int y,
                          Supplier<ItemStack> stackSupplier,
                          IntSupplier countSupplier,
                          IntConsumer onClick,
                          MutableComponent contents) {
        super(x, y, ITEM_SIZE, ITEM_SIZE, Component.empty());
        this.stackSupplier = stackSupplier;
        this.countSupplier = countSupplier;
        this.onClick = onClick;
        this.contents = contents;
    }


    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ItemStack stack = stackSupplier.get();
        if (stack.isEmpty()) return;

        // draw item
        g.renderItem(stack, getX(), getY());

        // draw count
        String txt = String.valueOf(countSupplier.getAsInt());
        g.drawString(Minecraft.getInstance().font, "x"+ txt,
                getX() + COUNT_PAD_X, getY() + TEXT_Y_OFF, 0xFFFFFF, false);

        // tooltip
        if (isHoveredOrFocused()) {
            List<Component> lines = stack.getTooltipLines(
                    Item.TooltipContext.of(Minecraft.getInstance().player.level()),
                    Minecraft.getInstance().player,
                    Minecraft.getInstance().options.advancedItemTooltips
                            ? TooltipFlag.Default.ADVANCED
                            : TooltipFlag.Default.NORMAL);
            lines.add(contents);
            g.renderTooltip(Minecraft.getInstance().font,
                    lines, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public void setFocused(boolean focused) {
        // we don't want it to be focused.
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 1 || button == 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            onClick.accept(button);   // 0 = left, 1 = right
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        /*ItemStack stack = stackSupplier.get();
        if (!stack.isEmpty()) {

            narration.add(NarratedElementType.TITLE,
                    Component.translatable("narration.shoppy.icon",
                            stack.getHoverName(),
                            countSupplier.getAsInt()));
        }*/
    }
}
