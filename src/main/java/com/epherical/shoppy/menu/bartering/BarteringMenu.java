package com.epherical.shoppy.menu.bartering;

import com.epherical.shoppy.Shoppy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class BarteringMenu extends AbstractContainerMenu {



    private final BlockPos blockPos;

    public BarteringMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(Shoppy.BARTERING_MENU.get(), id, buf.readBlockPos());
    }

    public BarteringMenu(@Nullable MenuType<?> type, int id, BlockPos blockPos) {
        super(type, id);
        this.blockPos = blockPos;
    }

    public static BarteringMenu barteringMenu(int id, BlockPos blockEntity) {
        return new BarteringMenu(Shoppy.BARTERING_MENU.get(), id, blockEntity); // registry accessor
    }


    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        return ItemStack.EMPTY;
    }

    @Override public boolean stillValid(Player player) {
        return true;
    }
}
