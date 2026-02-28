package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.CreativeTradingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeTradingBlock extends TradingBlock {

    public static final MapCodec<CreativeTradingBlock> CODEC = simpleCodec(CreativeTradingBlock::new);

    public CreativeTradingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreativeTradingBlockEntity(blockPos, blockState);
    }
}
