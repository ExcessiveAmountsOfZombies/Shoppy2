package com.epherical.shoppy.client.widget;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.network.payloads.ToggleAutomationPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 16 × 16 sprite button that toggles automatic item insertion for a bartering
 * block.  The sprite reflects the current state, and clicking it both updates
 * the client-side value immediately and sends a
 * {@link ToggleAutomationPayload} with {@code Target.INSERT}.
 */
public class InsertToggleButton extends Button {

    private static final ResourceLocation SPRITE_ENABLED  =
            ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/insert_enabled");
    private static final ResourceLocation SPRITE_DISABLED =
            ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/insert_disabled");

    private static final ResourceLocation SPRITE_ENABLED_HOVERED  =
            ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/insert_enabled_hovered");
    private static final ResourceLocation SPRITE_DISABLED_HOVERED =
            ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/insert_disabled_hovered");

    private final BarteringBlockEntity bartering;

    public InsertToggleButton(int x, int y, BarteringBlockEntity be) {
        super(x, y, 16, 16, Component.empty(),
              btn -> {
                  PacketDistributor.sendToServer(
                          new ToggleAutomationPayload(be.getBlockPos(),
                                                       ToggleAutomationPayload.Target.INSERT));
                  /* instant client feedback – server will sync shortly */
                  be.setAllowInsert(!be.isInsertAllowed());
              },
              DEFAULT_NARRATION);

        this.bartering = be;
        refreshTooltip();
    }

    /* ------------------------------------------------------------------ */
    /*  Rendering                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ResourceLocation sprite = bartering.isInsertAllowed()
                                  ? SPRITE_ENABLED
                                  : SPRITE_DISABLED;

        g.blitSprite(sprite, getX(), getY(), 16, 16);

        if (isHovered()) {
            sprite = bartering.isInsertAllowed()
                     ? SPRITE_ENABLED_HOVERED
                     : SPRITE_DISABLED_HOVERED;
            g.blitSprite(sprite, getX(), getY(), 16, 16);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                            */
    /* ------------------------------------------------------------------ */

    private void refreshTooltip() {
        setTooltip(Tooltip.create(Component.translatable(
                bartering.isInsertAllowed()
                ? "tooltip.shoppy.insert_enabled"
                : "tooltip.shoppy.insert_disabled")));
    }

    @Override
    public void onPress() {
        super.onPress();
        refreshTooltip();
    }
}