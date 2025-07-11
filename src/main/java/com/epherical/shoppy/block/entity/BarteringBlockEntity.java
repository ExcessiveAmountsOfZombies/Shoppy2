package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenu;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.epherical.shoppy.menu.bartering.BarteringMenuOwner.realContainer;

public class BarteringBlockEntity extends BaseContainerBlockEntity implements Clearable, MenuConstructor, Container, Ownable {

    /* slot layout -------------------------------------------------------- */
    public static final int SELLING_SLOT          = 0;
    public static final int FIRST_SHOP_SLOT       = 1;
    public static final int SHOP_SLOT_COUNT       = 15;
    public static final int FIRST_CURRENCY_SLOT   = FIRST_SHOP_SLOT + SHOP_SLOT_COUNT; // 16
    public static final int CURRENCY_SLOT_COUNT   = 27;
    public static final int CONTAINER_SIZE        = 1 + SHOP_SLOT_COUNT + CURRENCY_SLOT_COUNT; // 43

    /* data ----------------------------------------------------------------*/
    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    /* runtime bookkeeping -------------------------------------------------*/
    private ItemStack currency           = ItemStack.EMPTY;  // cached copy of slot 16
    private int        currencyStored    = 0;                // for data-sync convenience
    private int        storedSellingItems = 0;               // ditto

    protected UUID owner = Util.NIL_UUID;


    /* 4-int data array sent to client GUIs */
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
    };

    /* construction --------------------------------------------------------*/
    public BarteringBlockEntity(BlockPos pos, BlockState state) {
        super(Shoppy.BARTERING_STATION_ENTITY.get(), pos, state); // replace with your registry accessor
    }

    public BarteringBlockEntity(BlockEntityType<?> blockEntity, BlockPos blockPos, BlockState blockState) {
        super(blockEntity, blockPos, blockState);
    }

    /* BaseContainerBlockEntity -------------------------------------------*/
    @Override public int             getContainerSize() { return CONTAINER_SIZE; }
    @Override protected NonNullList<ItemStack> getItems()               { return items; }
    @Override protected void            setItems(NonNullList<ItemStack> list){ items.clear(); items.addAll(list); }

    /* save / load ---------------------------------------------------------*/

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.shoppy.bartering_station").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));

    }


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);

    }

    /* network sync --------------------------------------------------------*/
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    /*@Override public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }*/

    /* container logic -----------------------------------------------------*/
    @Override public boolean canPlaceItem(int idx, ItemStack stack) {
        if (idx == SELLING_SLOT)   return true; // player decides what to sell
        if (idx >= FIRST_SHOP_SLOT && idx < FIRST_CURRENCY_SLOT) {
            ItemStack filter = items.get(SELLING_SLOT);
            return !filter.isEmpty() && ItemStack.isSameItem(stack, filter);
        }
        // currency slots are private
        return false;
    }


    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return slot != SELLING_SLOT && slot < FIRST_CURRENCY_SLOT;
    }

    @Override public void clearContent() {
        items.clear();
    }



    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (getOwner().equals(player.getUUID())) {
            return realContainer(i, inventory, this, dataAccess);
        } else {
            return BarteringMenu.realContainer(i, inventory, this, dataAccess);
        }
    }

    /**
     * UNUSED DO NOT USE
     * @return ALWAYS RETURNS NULL.
     */
    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return null;
    }

    /* helpers -------------------------------------------------------------*/
    public boolean isCurrencySlot(int idx) {
        return idx >= FIRST_CURRENCY_SLOT;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }
}
