package com.epherical.shoppy.client;

import com.epherical.shoppy.client.screens.ShopPickingCreativeInventoryScreen;
import com.epherical.shoppy.network.payloads.SetSaleItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ShoppyCreativeSlot extends Slot {

    public ShoppyCreativeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        // todo; send a packet to the server
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen instanceof ShopPickingCreativeInventoryScreen screen1) {
            ItemStack stack = this.getItem();
            PacketDistributor.sendToServer(new SetSaleItemPayload(stack, screen1.isCurrency()));
            player.closeContainer();
        }

        return false;
    }
}
