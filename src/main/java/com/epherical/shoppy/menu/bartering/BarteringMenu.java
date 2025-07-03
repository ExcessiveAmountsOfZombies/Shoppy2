package com.epherical.shoppy.menu.bartering;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.menu.AbstractShoppyMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BarteringMenu extends AbstractShoppyMenu {


    protected Container container;

    public static BarteringMenu realContainer(int pContainerId, Inventory playerInventory, Container container, ContainerData containerData) {
        return new BarteringMenu(Shoppy.BARTERING_MENU.get(), pContainerId, playerInventory, container, containerData);
    }

    public BarteringMenu(int pContainerId, Inventory playerInventory) {
        this(Shoppy.BARTERING_MENU.get(), pContainerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(4));
    }

    public BarteringMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Container container, ContainerData data) {
        super(pMenuType, pContainerId, playerInventory, data);
        this.container = container;




        for(int rows = 0; rows < 3; ++rows) {
            for(int slots = 0; slots < 9; ++slots) {
                this.addSlot(new Slot(playerInventory, slots + rows * 9 + 9, 8 + slots * 18, 65 + rows * 18));
            }
        }

        for(int slots = 0; slots < 9; ++slots) {
            this.addSlot(new Slot(playerInventory, slots, 8 + slots * 18, 123));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public Container getContainer() {
        return container;
    }
}
