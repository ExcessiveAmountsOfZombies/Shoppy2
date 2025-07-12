package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BarteringBlockEntity extends BlockEntity implements Nameable, MenuProvider, MenuConstructor, Ownable {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    private ItemStack saleItem = ItemStack.EMPTY;   // the single item-type being sold
    private ItemStack currency = ItemStack.EMPTY;   // the single item-type accepted


    /* three different bundle sizes (same item/currency, different counts)  */
    private final int[] saleCounts = new int[3];   // how many items the customer receives
    private final int[] costCounts = new int[3];   // how many currency items are required

    public ItemStack getSaleItem() {
        return saleItem;
    }

    public ItemStack getCurrencyItem() {
        return currency;
    }

    public int getSaleCount(int idx) {
        return saleCounts[idx];
    }

    public int getCostCount(int idx) {
        return costCounts[idx];
    }

    public int getStock() {
        return inventory.get(0).getCount();
    }


    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    protected UUID owner = Util.NIL_UUID;


    /* 4-int data array sent to client GUIs *//*
    private final ContainerData dataAccess = new ContainerData() {
        @Override public int get(int id)  {
            return switch (id) {
                case 0 -> currency.getCount();
                case 1 -> getItem(SELLING_SLOT).getCount();
                case 2 -> currencyStored;
                case 3 -> storedSellingItems;
                default -> 0;
            };
        }
        @Override public void set(int id, int value) {
            switch (id) {
                case 0 -> currency.setCount(value);
                case 1 -> getItem(SELLING_SLOT).setCount(value);
                case 2 -> currencyStored = value;
                case 3 -> storedSellingItems = value;
            }
        }
        @Override public int getCount() { return 4; }
    };*/

    public BarteringBlockEntity(BlockPos pos, BlockState state) {
        super(Shoppy.BARTERING_STATION_ENTITY.get(), pos, state); // replace with your registry accessor
    }

    public BarteringBlockEntity(BlockEntityType<?> blockEntity, BlockPos blockPos, BlockState blockState) {
        super(blockEntity, blockPos, blockState);
    }


    public void tryPurchase(Player player, int offerIdx) {
        if (offerIdx < 0 || offerIdx > 2) return;
        int sale = saleCounts[offerIdx];
        int cost = costCounts[offerIdx];

        if (sale <= 0 || cost <= 0) return;
        if (getStock() < sale) return;                                   // no stock
        if (!player.getInventory().contains(new ItemStack(currency.getItem(), cost))) return; // no money

        // remove money from player
        //ItemHandlerHelper.insertItem(player.getInventory(), currency, cost, false);


        // give products
        ItemStack toGive = saleItem.copy();
        toGive.setCount(sale);
        ItemHandlerHelper.giveItemToPlayer(player, toGive);

        // consume stock, add money to revenue slot
        inventory.get(0).shrink(sale);
        ItemStack output = inventory.get(1);
        if (output.isEmpty())
            inventory.set(1, new ItemStack(currency.getItem(), cost));
        else
            output.grow(cost);

        setChanged();
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // ContainerHelper.saveAllItems(tag, items, registries);
    }


    protected Component getDefaultName() {
        return Component.translatable("block.shoppy.bartering_station").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
    }


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        //ContainerHelper.loadAllItems(tag, items, registries);

    }

    /* network sync --------------------------------------------------------*/
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
        // todo; this will update it in the client
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (getOwner().equals(player.getUUID())) {
            return BarteringMenuOwner.barteringOwner(i, getBlockPos(), false);
        } else {
            return BarteringMenu.barteringMenu(i, getBlockPos());
        }
    }



    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public Component getName() {
        return getDefaultName();
    }

    @Override
    public boolean hasCustomName() {
        return Nameable.super.hasCustomName();
    }

    @Override
    public Component getDisplayName() {
        return Nameable.super.getDisplayName();
    }

    @Override
    public @Nullable Component getCustomName() {
        return Nameable.super.getCustomName();
    }
}
