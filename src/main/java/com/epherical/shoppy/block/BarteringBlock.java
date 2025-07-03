package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BarteringBlock extends AbstractTradingBlock {

    public static final MapCodec<BarteringBlock> CODEC = simpleCodec(BarteringBlock::new);

    public BarteringBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BarteringBlockEntity(blockPos, blockState);
    }
}
