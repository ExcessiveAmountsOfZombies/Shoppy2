package com.epherical.shoppy.network.payloads;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.epherical.shoppy.Shoppy.MODID;

/**
 * Sent by the client when the owner clicks the automation-toggle buttons.
 * The server simply flips the requested flag in the {@link BarteringBlockEntity}.
 */
public record ToggleAutomationPayload(BlockPos pos, Target target) implements CustomPacketPayload {

    public enum Target { INSERT, EXTRACT }

    public static final Type<ToggleAutomationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "toggle_automation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAutomationPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ToggleAutomationPayload::pos,
                    ByteBufCodecs.VAR_INT,   p -> p.target().ordinal(),
                    (blockPos, integer) -> new ToggleAutomationPayload(blockPos, Target.class.getEnumConstants()[integer])
            );

    @Override
    public @NotNull Type<ToggleAutomationPayload> type() {
        return TYPE;
    }
}
