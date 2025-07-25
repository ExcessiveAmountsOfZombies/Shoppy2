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
 * 16 × 16 sprite button shown in the owner GUI.<br>
 * The sprite reflects the current “allow-extract” state and clicking it
 * immediately toggles the flag locally and via {@link ToggleAutomationPayload}.
 */
public class ExtractToggleButton extends Button {

    private static final ResourceLocation SPRITE_ENABLED = ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/extract_enabled");
    private static final ResourceLocation SPRITE_DISABLED = ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/extract_disabled");

    private static final ResourceLocation SPRITE_ENABLED_HOVERED = ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/extract_enabled_hovered");
    private static final ResourceLocation SPRITE_DISABLED_HOVERED = ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "widget/extract_disabled_hovered");

    private final BarteringBlockEntity bartering;

    public ExtractToggleButton(int x, int y, BarteringBlockEntity be) {
        super(x, y, 16, 16, Component.empty(), btn -> {
            PacketDistributor.sendToServer(
                    new ToggleAutomationPayload(be.getBlockPos(),
                            ToggleAutomationPayload.Target.EXTRACT));
            be.setAllowExtract(!be.isExtractAllowed());
        }, DEFAULT_NARRATION);

        this.bartering = be;
        refreshTooltip();
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ResourceLocation sprite = bartering.isExtractAllowed()
                ? SPRITE_ENABLED
                : SPRITE_DISABLED;

        /* draw the 16×16 sprite */
        g.blitSprite(sprite, getX(), getY(), 16, 16);

        /* standard hover overlay (slightly darken) */
        if (isHovered()) {
            sprite = bartering.isExtractAllowed()
                    ? SPRITE_ENABLED_HOVERED
                    : SPRITE_DISABLED_HOVERED;
            g.blitSprite(sprite, getX(), getY(), 16, 16);
        }
    }

    /**
     * Updates the tooltip text according to the current state.
     */
    private void refreshTooltip() {
        setTooltip(Tooltip.create(Component.translatable(
                bartering.isExtractAllowed()
                        ? "tooltip.shoppy.extract_enabled"
                        : "tooltip.shoppy.extract_disabled")));
    }

    @Override
    public void onPress() {
        super.onPress();
        refreshTooltip();
    }
}
