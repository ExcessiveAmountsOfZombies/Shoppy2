package com.epherical.shoppy.client.screens;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.flag.FeatureFlagSet;

public class ShopPickingCreativeInventoryScreen extends CreativeModeInventoryScreen {


    public ShopPickingCreativeInventoryScreen(LocalPlayer player, FeatureFlagSet enabledFeatures, boolean displayOperatorCreativeTab) {
        super(player, enabledFeatures, displayOperatorCreativeTab);
    }
}
