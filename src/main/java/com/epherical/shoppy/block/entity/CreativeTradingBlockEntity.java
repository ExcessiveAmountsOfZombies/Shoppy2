package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeTradingBlockEntity extends TradingBlockEntity {

    public CreativeTradingBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Shoppy.CREATIVE_TRADING_SHOP_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected @Nullable Component getDefaultName() {
        return Component.translatable("block.shoppy.creative_trading_shop").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
    }

    @Override
    public int getSaleItemCount() {
        return 100000;
    }

    @Override
    public int getFreeSlots() {
        return 100000000;
    }

    @Override
    public void addSaleItems(int n) {
        // Infinite stock.
    }

    @Override
    public void removeSaleItems(int n) {
        // Infinite stock.
    }

    @Override
    public boolean hasUnlimitedStock() {
        return true;
    }
}
