package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
                case 0 -> getSaleItemCount();
                case 1 -> getCurrencyItemCount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> setSaleItemCount(value);
                case 1 -> setCurrencyItemCount(value);
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
        if (getSaleItemCount() < saleQty) return;


        ItemStack currencyTemplate = new ItemStack(currency.getItem());
        int playerCurrency = 0;

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (ItemStack.isSameItem(stack, currencyTemplate)) {
                playerCurrency += stack.getCount();
                if (playerCurrency >= costQty) break; // early exit
            }
        }
        if (playerCurrency < costQty) return;  // insufficient funds

        int remaining = costQty;
        for (int i = 0; i < player.getInventory().getNonEquipmentItems().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getNonEquipmentItems().get(i);
            if (!ItemStack.isSameItem(stack, currencyTemplate)) continue;

            int toTake = Math.min(stack.getCount(), remaining);
            stack.shrink(toTake);
            if (stack.isEmpty()) player.getInventory().getNonEquipmentItems().set(i, ItemStack.EMPTY);
            remaining -= toTake;
        }
        if (remaining > 0) return;

        ItemStack product = saleItem.copy();
        product.setCount(saleQty);
        ItemHandlerHelper.giveItemToPlayer(player, product);

        saleItemCount = Math.max(0, saleItemCount - saleQty);
        addCurrencyItems(costQty);

        setChanged();
    }

    private void writeNBT(ValueOutput out) {
        if (!saleItem.isEmpty()) {
            out.store("SaleItem", ItemStack.CODEC, saleItem);
        }
        if (!currency.isEmpty()) {
            out.store("Currency", ItemStack.CODEC, currency);
        }

        out.putIntArray("SaleCounts", saleCounts);
        out.putIntArray("CostCounts", costCounts);

        out.putInt("SaleItemCount", saleItemCount);
        out.putInt("CurrencyItemCount", currencyItemCount);

        out.storeNullable("Owner", UUIDUtil.CODEC, owner.equals(Util.NIL_UUID) ? null : owner);
    }

    private void readNBT(ValueInput in) {
        saleItem = in.read("SaleItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        currency = in.read("Currency", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        in.getIntArray("SaleCounts")
                .ifPresent(a -> System.arraycopy(a, 0, saleCounts, 0, Math.min(3, a.length)));
        in.getIntArray("CostCounts")
                .ifPresent(a -> System.arraycopy(a, 0, costCounts, 0, Math.min(3, a.length)));

        saleItemCount = in.getIntOr("SaleItemCount", 0);
        currencyItemCount = in.getIntOr("CurrencyItemCount", 0);

        in.read("Owner", UUIDUtil.CODEC).ifPresent(u -> owner = u);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        writeNBT(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        readNBT(in);
    }

    @Override
    public void handleUpdateTag(ValueInput in) {
        readNBT(in);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
        writeNBT(output);
        return output.buildResult();
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


    public void setCurrencyItemCount(int currencyItemCount) {
        this.currencyItemCount = currencyItemCount;
    }

    public void setSaleItemCount(int saleItemCount) {
        this.saleItemCount = saleItemCount;
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


    /**
     * Spawns item entities for every stored sale / currency stack.
     * Called when the block is broken or replaced.
     */
    public void dropStock(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        dropStack(level, pos, saleItem, saleItemCount);
        dropStack(level, pos, currency, currencyItemCount);

        saleItemCount = 0;
        currencyItemCount = 0;
        setChanged();
    }

    private static void dropStack(Level level, BlockPos pos, ItemStack prototype, int count) {
        if (count <= 0 || prototype.isEmpty()) return;

        int max = prototype.getMaxStackSize();
        int left = count;
        while (left > 0) {
            int n = Math.min(max, left);
            ItemStack stack = prototype.copy();
            stack.setCount(n);
            Containers.dropItemStack(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    stack);
            left -= n;
        }
    }
}
