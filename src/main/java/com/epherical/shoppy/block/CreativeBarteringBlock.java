package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeBarteringBlock extends BarteringBlock {

    public static final MapCodec<CreativeBarteringBlock> CODEC = simpleCodec(CreativeBarteringBlock::new);


    public CreativeBarteringBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreativeBarteringBlockEntity(blockPos, blockState);
    }
}
