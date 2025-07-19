package com.epherical.shoppy.network.payloads;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.epherical.shoppy.Shoppy.MODID;

public record StockTransferPayload(BlockPos pos, boolean saleSide, boolean insert)
        implements CustomPacketPayload {

    public static final Type<StockTransferPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "stock_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StockTransferPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, StockTransferPayload::pos,
                    ByteBufCodecs.BOOL, StockTransferPayload::saleSide,
                    ByteBufCodecs.BOOL, StockTransferPayload::insert,
                    StockTransferPayload::new
            );

    @Override
    public @NotNull Type<StockTransferPayload> type() {
        return TYPE;
    }
}
