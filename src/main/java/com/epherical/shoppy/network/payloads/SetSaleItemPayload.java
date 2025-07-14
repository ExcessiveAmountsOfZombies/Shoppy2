package com.epherical.shoppy.network.payloads;

import com.epherical.shoppy.Shoppy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Sent from client ➜ server when the owner chooses a new item to sell.
 *
 * @param stack the ItemStack that should become the shop’s sale item
 */
public record SetSaleItemPayload(ItemStack stack, boolean currency) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            Shoppy.MODID, "set_sale_item");

    public static final Type<SetSaleItemPayload> TYPE = new Type<>(ID);

    /* ─────────────────────────────────────────  codec ──────────────────── */
    public static final StreamCodec<RegistryFriendlyByteBuf, SetSaleItemPayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,      SetSaleItemPayload::stack,
            ByteBufCodecs.BOOL,          SetSaleItemPayload::currency,
            SetSaleItemPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
