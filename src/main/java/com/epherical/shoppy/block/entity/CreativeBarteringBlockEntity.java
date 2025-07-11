package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CreativeBarteringBlockEntity extends BarteringBlockEntity implements Ownable {


    public CreativeBarteringBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Shoppy.CREATIVE_BARTERING_STATION_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected @Nullable Component getDefaultName() {
        return Component.translatable("block.shoppy.creative_bartering_station").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
    }



    @Override
    public void setOwner(UUID owner) {

    }

    @Override
    public UUID getOwner() {
        return null;
    }
}
