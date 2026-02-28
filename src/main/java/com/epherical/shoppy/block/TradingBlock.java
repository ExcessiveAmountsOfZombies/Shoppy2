package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.TradingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TradingBlock extends AbstractTradingBlock {

    public static final MapCodec<TradingBlock> CODEC = simpleCodec(TradingBlock::new);

    public TradingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TradingBlockEntity(blockPos, blockState);
    }
}
