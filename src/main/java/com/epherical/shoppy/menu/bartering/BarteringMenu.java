package com.epherical.shoppy.menu.bartering;

import com.epherical.shoppy.Shoppy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class BarteringMenu extends AbstractContainerMenu {



    private final BlockPos blockPos;

    private final ContainerData containerData;

    public BarteringMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(Shoppy.BARTERING_MENU.get(), id, buf.readBlockPos(), new SimpleContainerData(2));
    }

    public BarteringMenu(@Nullable MenuType<?> type, int id, BlockPos blockPos, ContainerData data) {
        super(type, id);
        this.blockPos = blockPos;
        this.containerData = data;

        addDataSlots(containerData);
    }

    public static BarteringMenu barteringMenu(int id, BlockPos blockEntity, ContainerData containerData) {
        return new BarteringMenu(Shoppy.BARTERING_MENU.get(), id, blockEntity, containerData); // registry accessor
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


    public ContainerData getContainerData() {
        return containerData;
    }
}
