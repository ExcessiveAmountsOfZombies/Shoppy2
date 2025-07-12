package com.epherical.shoppy.client;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShoppyCreativeSlot extends Slot {

    public ShoppyCreativeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        // todo; send a packet to the server
        ItemStack stack = this.getItem();
        return false;
    }
}
