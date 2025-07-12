package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.client.widget.AddItemButton;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.AddItemRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.epherical.shoppy.Shoppy.MODID;

public class BarteringScreenOwner extends AbstractContainerScreen<BarteringMenuOwner> {

    public static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/sprites/container/bartering_page_owner.png");


    private static final Component ADD_ITEM_BTN = Component.translatable("screen.shoppy.add_item");


    public BarteringScreenOwner(BarteringMenuOwner pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = -3000;
    }

    @Override
    protected void init() {
        super.init();

        if (!getMenu().isEditing()) {
            /* centre the button horizontally, place it near the bottom of the vanilla texture */
            int btnX = leftPos + (imageWidth - 150) / 2;   // 80 px wide
            int btnY = topPos  + 25;                     // tweak if texture height changes

            this.addRenderableWidget(AddItemButton.addItem(ADD_ITEM_BTN,  button -> {
                PacketDistributor.sendToServer(new AddItemRequestPayload(menu.getBlockPos()));
            }).size(60, 20).pos(btnX, btnY).build());
        } else {

        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(graphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = leftPos;
        int top = topPos;
        graphics.blit(CONTAINER_BACKGROUND, left, top, 0, 0, 176, 191);
        //graphics.drawString(font, "Stored", leftPos + 10, topPos + 50, 0xFFFFFF);
        //graphics.drawString(font, "Stored", leftPos + 132, topPos + 54, 0xFFFFFF);
        //this.blit(pPoseStack, left + 79, top + 34, 0, 126, this.imageWidth, 16);
    }
}
