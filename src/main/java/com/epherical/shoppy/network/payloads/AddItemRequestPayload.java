package com.epherical.shoppy.network.payloads;

import com.epherical.shoppy.Shoppy;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AddItemRequestPayload(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "add_item_request");

    public static final Type<AddItemRequestPayload> TYPE = new Type<>(ID);


    public static final StreamCodec<ByteBuf, AddItemRequestPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AddItemRequestPayload::pos,
            AddItemRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
