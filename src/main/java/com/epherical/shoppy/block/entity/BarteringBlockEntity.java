package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BarteringBlockEntity extends BlockEntity implements Nameable, MenuProvider, MenuConstructor, Ownable {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    private ItemStack saleItem = ItemStack.EMPTY;   // the single item-type being sold
    private ItemStack currency = ItemStack.EMPTY;   // the single item-type accepted

    private final int[] saleCounts = new int[3];   // how many items the customer receives
    private final int[] costCounts = new int[3];   // how many currency items are required

    private int saleItemCount = 0;
    private int currencyItemCount = 0;

    protected UUID owner = Util.NIL_UUID;


    private static final int SLOTS = 1024;


    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> saleItemCount;
                case 1 -> currencyItemCount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> saleItemCount = value;
                case 1 -> currencyItemCount = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    /* put anywhere inside the class */
    public void setOffer(int idx, int cost, int sale) {
        if (idx < 0 || idx >= saleCounts.length) return;
        costCounts[idx] = cost;
        saleCounts[idx] = sale;
    }

    public BarteringBlockEntity(BlockPos pos, BlockState state) {
        super(Shoppy.BARTERING_STATION_ENTITY.get(), pos, state); // replace with your registry accessor
    }

    public BarteringBlockEntity(BlockEntityType<?> blockEntity, BlockPos blockPos, BlockState blockState) {
        super(blockEntity, blockPos, blockState);
    }


    public void tryPurchase(Player player, int offerIdx) {
        if (offerIdx < 0 || offerIdx > 2) return;

        int saleQty = saleCounts[offerIdx];
        int costQty = costCounts[offerIdx];

        if (saleQty <= 0 || costQty <= 0) return;
        if (saleItemCount < saleQty) return;


        ItemStack currencyTemplate = new ItemStack(currency.getItem());
        int playerCurrency = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItem(stack, currencyTemplate)) {
                playerCurrency += stack.getCount();
                if (playerCurrency >= costQty) break; // early exit
            }
        }
        if (playerCurrency < costQty) return;  // insufficient funds

        int remaining = costQty;
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!ItemStack.isSameItem(stack, currencyTemplate)) continue;

            int toTake = Math.min(stack.getCount(), remaining);
            stack.shrink(toTake);
            if (stack.isEmpty()) player.getInventory().items.set(i, ItemStack.EMPTY);
            remaining -= toTake;
        }
        if (remaining > 0) return;

        ItemStack product = saleItem.copy();
        product.setCount(saleQty);
        ItemHandlerHelper.giveItemToPlayer(player, product);

        saleItemCount = Math.max(0, saleItemCount - saleQty);
        currencyItemCount += costQty;

        setChanged();
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeNBT(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readNBT(tag, registries);
    }


    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        readNBT(tag, lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag updateTag = super.getUpdateTag(registries);
        writeNBT(updateTag, registries);
        return updateTag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    protected Component getDefaultName() {
        return Component.translatable("block.shoppy.bartering_station").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (getOwner().equals(player.getUUID())) {
            return BarteringMenuOwner.barteringOwner(i, getBlockPos(), false, data);
        } else {
            return BarteringMenu.barteringMenu(i, getBlockPos(), data);
        }
    }

    private void writeNBT(CompoundTag tag, HolderLookup.Provider registries) {
        // ContainerHelper.saveAllItems(tag, inventory, registries);   // no longer used for counts

        if (!saleItem.isEmpty()) tag.put("SaleItem", saleItem.save(registries));
        if (!currency.isEmpty()) tag.put("Currency", currency.save(registries));

        tag.put("SaleCounts", new IntArrayTag(saleCounts));
        tag.put("CostCounts", new IntArrayTag(costCounts));

        tag.putInt("SaleItemCount", saleItemCount);
        tag.putInt("CurrencyItemCount", currencyItemCount);

        if (!owner.equals(Util.NIL_UUID)) tag.putUUID("Owner", owner);
    }

    private void readNBT(CompoundTag tag, HolderLookup.Provider registries) {

        if (tag.contains("SaleItem"))
            saleItem = ItemStack.parse(registries, tag.getCompound("SaleItem")).orElse(ItemStack.EMPTY);
        if (tag.contains("Currency"))
            currency = ItemStack.parse(registries, tag.getCompound("Currency")).orElse(ItemStack.EMPTY);

        if (tag.contains("SaleCounts", IntArrayTag.TAG_INT_ARRAY))
            System.arraycopy(tag.getIntArray("SaleCounts"), 0, saleCounts, 0, Math.min(3, tag.getIntArray("SaleCounts").length));
        if (tag.contains("CostCounts", IntArrayTag.TAG_INT_ARRAY))
            System.arraycopy(tag.getIntArray("CostCounts"), 0, costCounts, 0, Math.min(3, tag.getIntArray("CostCounts").length));

        saleItemCount = tag.getInt("SaleItemCount");
        currencyItemCount = tag.getInt("CurrencyItemCount");

        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
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

    public int getFreeSlots() {
        return SLOTS - getSaleItemCount();
    }

    public int getSaleItemCount() {
        return saleItemCount;
    }

    public int getCurrencyItemCount() {
        return currencyItemCount;
    }

    public void addSaleItems(int n) {
        saleItemCount += n;
        setChanged();
    }

    public void removeSaleItems(int n) {
        saleItemCount = Math.max(0, saleItemCount - n);
        setChanged();
    }

    public void addCurrencyItems(int n) {
        currencyItemCount += n;
        setChanged();
    }

    public void removeCurrencyItems(int n) {
        currencyItemCount = Math.max(0, currencyItemCount - n);
        setChanged();
    }

    public void setSaleItem(ItemStack saleItem) {
        this.saleItem = saleItem;
    }

    public void setCurrency(ItemStack currency) {
        this.currency = currency;
    }

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
        return saleItemCount;
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public ContainerData getContainerData() {
        return data;
    }

}
