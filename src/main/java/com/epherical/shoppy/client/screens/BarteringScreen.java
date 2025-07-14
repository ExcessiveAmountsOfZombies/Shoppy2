package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.client.widget.RowButton;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.epherical.shoppy.Shoppy.MODID;

public class BarteringScreen extends AbstractContainerScreen<BarteringMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/sprites/container/bartering_page_owner.png");

    private static final int ROW_START_Y = 30;   // first row offset from topPos
    private static final int ROW_HEIGHT = 20;   // distance between rows
    private static final int ROW_PAD_X = 8;    // left / right padding inside BG
    private static final int TEXT_Y_OFF = 6;    // text offset inside a row
    private static final int ITEM_SIZE = 16;


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

            addRenderableWidget(new RowButton(x, y, width, ROW_HEIGHT - 2, i, bartering));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(graphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = leftPos;
        int top = topPos;
        graphics.blit(CONTAINER_BACKGROUND, left, top, 0, 0, 176, 191);
    }
}
