package com.epherical.shoppy.network.payloads;

import com.epherical.shoppy.Shoppy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent when the owner finishes editing the offer-price text boxes.
 *
 * @param offerIndex index 0‒2 of the offer that is being edited
 * @param price      currency items required (0 = discard server-side)
 * @param received   items the buyer will get
 */
public record PriceSubmissionPayload(int offerIndex, int price, int received) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "price_submission");

    public static final Type<PriceSubmissionPayload> TYPE = new Type<>(ID);

    /* ───────────────────────────── codec ───────────────────────────── */
    public static final StreamCodec<RegistryFriendlyByteBuf, PriceSubmissionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PriceSubmissionPayload::offerIndex,
            ByteBufCodecs.INT, PriceSubmissionPayload::price,
            ByteBufCodecs.INT, PriceSubmissionPayload::received,
            PriceSubmissionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
