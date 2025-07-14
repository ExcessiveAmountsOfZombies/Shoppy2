package com.epherical.shoppy.client.screens;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.flag.FeatureFlagSet;

public class ShopPickingCreativeInventoryScreen extends CreativeModeInventoryScreen {

    private final boolean currency;

    public ShopPickingCreativeInventoryScreen(LocalPlayer player, FeatureFlagSet enabledFeatures, boolean displayOperatorCreativeTab, boolean currency) {
        super(player, enabledFeatures, displayOperatorCreativeTab);
        this.currency = currency;
    }

    public boolean isCurrency() {
        return currency;
    }
}
