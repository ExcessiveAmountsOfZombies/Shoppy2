package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.client.widget.AddItemButton;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.AddItemRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.epherical.shoppy.Shoppy.MODID;

public class BarteringScreenOwner extends AbstractContainerScreen<BarteringMenuOwner> {

    public static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/sprites/container/bartering_page_owner.png");


    private static final Component ADD_ITEM_BTN = Component.translatable("screen.shoppy.add_price");


    public BarteringScreenOwner(BarteringMenuOwner pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = -3000;
    }

    @Override
    protected void init() {
        super.init();

        // todo; place the button lower once the items are in place...
        int btnX = leftPos + (imageWidth - 150) / 2;
        int btnY = topPos + 25;
        if (!getMenu().isEditing()) {
            this.addRenderableWidget(AddItemButton.addItem(ADD_ITEM_BTN, button -> {
                PacketDistributor.sendToServer(new AddItemRequestPayload(menu.getBlockPos()));
            }).size(68, 14).pos(btnX, btnY).build());
        } else {
            EditBox price = this.addRenderableWidget(new EditBox(minecraft.font, btnX - 8, btnY + 2, 30, 14, Component.literal("Price")));
            price.setTooltip(Tooltip.create(Component.literal("Price -- The price that the player must pay to 'receive' any items.")));
            EditBox received = this.addRenderableWidget(new EditBox(minecraft.font, btnX + 24, btnY + 2, 30, 14, Component.literal("Received")));
            received.setTooltip(Tooltip.create(Component.literal("Received -- What the player will get in return for giving you the price of the item.")));
            Checkbox checkbox1 = this.addRenderableWidget(Checkbox.builder(Component.literal(""), minecraft.font)
                    .selected(true)
                    .pos(btnX + 30 + 25, btnY + 1)
                    .onValueChange((checkbox, value) -> {
                        // todo send a packet to the server
                        System.out.println("Hwhaha");
                    }).build());
            checkbox1.setTooltip(Tooltip.create(Component.literal("Submit the pricing.")));

        }

        addRenderableWidget(Button.builder(Component.translatable("screen.shoppy.set_item"), button -> {
            minecraft.setScreen(new ShopPickingCreativeInventoryScreen(minecraft.player, this.minecraft.player.connection.enabledFeatures(), false));
        }).size(68, 14).pos(btnX + 50, btnY + 100).build());

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
    }
}
