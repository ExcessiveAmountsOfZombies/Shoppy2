package com.epherical.shoppy.menu.bartering;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class BarteringMenu extends AbstractContainerMenu {

    private final Container blockInv;

    /* slot coords (feel free to tweak) */
    private static final int GUI_LEFT  = 8;
    private static final int GUI_TOP   = 18;

    /* factory used by BlockEntity */
    public static BarteringMenu realContainer(int id, Inventory playerInv, Container blockInv, ContainerData data) {
        return new BarteringMenu(Shoppy.BARTERING_MENU.get(), id, playerInv, blockInv, data); // registry accessor
    }

    public BarteringMenu(int id, Inventory playerInv) {
        this(Shoppy.BARTERING_MENU.get(), id, playerInv, new SimpleContainer(BarteringBlockEntity.CONTAINER_SIZE), new SimpleContainerData(43));
    }

    public BarteringMenu(@Nullable MenuType<?> type, int id, Inventory playerInv, Container blockInv, ContainerData data) {
        super(type, id);
        this.blockInv = blockInv;
        checkContainerSize(blockInv, BarteringBlockEntity.CONTAINER_SIZE);
        blockInv.startOpen(playerInv.player);
        addDataSlots(data);

        /* 1. Selling-item filter slot */
        this.addSlot(new Slot(blockInv, BarteringBlockEntity.SELLING_SLOT, 17, 39) {
            @Override public boolean mayPlace(ItemStack stack) { return true; }
        });

        /* 2. 15 shop slots (3 rows × 5 cols) */
        int index = BarteringBlockEntity.FIRST_SHOP_SLOT;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 5; ++col) {
                int x = 44 + col * 18;
                int y = 21  + row * 18;
                this.addSlot(new Slot(blockInv, index++, x, y) {
                    @Override public boolean mayPlace(ItemStack stack) {
                        return blockInv.canPlaceItem(getSlotIndex(), stack);
                    }
                });
            }
        }

        /* 3. Player inventory (27 + hotbar) */
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInv, col + row * 9 + 9,
                        GUI_LEFT + col * 18, 110 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInv, col, GUI_LEFT + col * 18, 167));
        }
    }

    /* shift-click transfer logic -----------------------------------------*/
    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        Slot slot = getSlot(idx);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original  = slot.getItem();
        ItemStack movedCopy = original.copy();

        int blockSlotEnd = BarteringBlockEntity.FIRST_CURRENCY_SLOT; // 16 (currency is not in menu)

        if (idx < blockSlotEnd) { // coming FROM block inventory -> player inv
            if (!moveItemStackTo(original, blockSlotEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else {                  // coming FROM player inventory
            // 1) try filter slot if empty
            if (blockInv.getItem(BarteringBlockEntity.SELLING_SLOT).isEmpty()) {
                if (!moveItemStackTo(original, BarteringBlockEntity.SELLING_SLOT,
                        BarteringBlockEntity.SELLING_SLOT + 1, false))
                    return ItemStack.EMPTY;
            } else { // 2) otherwise to shop storage if same item
                if (!moveItemStackTo(original,
                        BarteringBlockEntity.FIRST_SHOP_SLOT,
                        blockSlotEnd, false))
                    return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return movedCopy;
    }

    @Override public boolean stillValid(Player player) {
        return blockInv.stillValid(player);
    }

    @Override public void removed(Player player) {
        super.removed(player);
        blockInv.stopOpen(player);
    }
}
