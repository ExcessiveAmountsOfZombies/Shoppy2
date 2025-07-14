package com.epherical.shoppy.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import com.epherical.shoppy.Shoppy;

/**
 * Sent when a client clicks on one of the three offer rows
 * to request a purchase attempt.
 *
 * @param offerIndex index of the clicked offer (0‒2)
 */
public record PurchaseAttemptPayload(int offerIndex) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Shoppy.MODID, "purchase_attempt");

    public static final Type<PurchaseAttemptPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PurchaseAttemptPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, PurchaseAttemptPayload::offerIndex,
                                  PurchaseAttemptPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
