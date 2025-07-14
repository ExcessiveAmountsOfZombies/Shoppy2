package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.client.widget.AddItemButton;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.payloads.AddItemRequestPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

import static com.epherical.shoppy.Shoppy.MODID;

public class BarteringScreenOwner extends AbstractContainerScreen<BarteringMenuOwner> {

    private static final int ITEM_SIZE = 16;


    private static final int SALE_ITEM_X      = 26;   // pixels from leftPos
    private static final int SALE_ITEM_Y      = 48;   // pixels from topPos
    private static final int CURRENCY_ITEM_X  = 111;
    private static final int CURRENCY_ITEM_Y  = 133;



    private BarteringBlockEntity bartering;

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


        bartering = (BarteringBlockEntity) minecraft.level.getBlockEntity(menu.getBlockPos());

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
            minecraft.setScreen(new ShopPickingCreativeInventoryScreen(minecraft.player, this.minecraft.player.connection.enabledFeatures(), false, true));
        }).size(60, 14).pos(btnX + 98, btnY + 147).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.shoppy.set_sold"), button -> {
            minecraft.setScreen(new ShopPickingCreativeInventoryScreen(minecraft.player, this.minecraft.player.connection.enabledFeatures(), false, false));
        }).size(60, 14).pos(btnX + 98, btnY + 147 - 14).build());

    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        int left = leftPos;
        int top = topPos;
        this.renderTooltip(graphics, pMouseX, pMouseY);

        if (bartering != null) {
            ItemStack saleStack = bartering.getSaleItem();
            if (!saleStack.isEmpty()) {
                int x = left + CURRENCY_ITEM_X;
                int y = top  + CURRENCY_ITEM_Y - ITEM_SIZE;

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
                graphics.renderItemDecorations(this.font, saleStack, x, y);
            }

            ItemStack currencyStack = bartering.getCurrencyItem();
            if (!currencyStack.isEmpty()) {
                int x = left + CURRENCY_ITEM_X;
                int y = top  + CURRENCY_ITEM_Y;

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
                graphics.renderItemDecorations(this.font, currencyStack, x, y);
            }
        }
    }

    private static boolean isHovering(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + ITEM_SIZE
                && mouseY >= y && mouseY < y + ITEM_SIZE;
    }


    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = leftPos;
        int top = topPos;
        graphics.blit(CONTAINER_BACKGROUND, left, top, 0, 0, 176, 191);

    }
}
