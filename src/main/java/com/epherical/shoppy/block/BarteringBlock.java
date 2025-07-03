package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BarteringBlock extends AbstractTradingBlock {



    public BarteringBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BarteringBlockEntity(blockPos, blockState);
    }
}
