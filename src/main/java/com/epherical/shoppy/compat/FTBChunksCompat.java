package com.epherical.shoppy.compat;

import com.epherical.shoppy.block.AbstractTradingBlock;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.ftb.mods.ftbchunks.FTBCUtils;
import dev.ftb.mods.ftbchunks.api.Protection;
import dev.ftb.mods.ftbchunks.api.ProtectionPolicy;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl;
import dev.ftb.mods.ftbteams.api.event.TeamCollectPropertiesEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import dev.ftb.mods.ftbteams.api.property.BooleanProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class FTBChunksCompat {


    public FTBChunksCompat() {
        TeamEvent.COLLECT_PROPERTIES.register(this::teamConfig);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(this::click);
    }

    public static final BooleanProperty ALLOW_SHOP_USAGE =
            new BooleanProperty(ResourceLocation.fromNamespaceAndPath("shoppy", "allow_shop_usage"), true);



    Protection USE_SHOP = (serverPlayer, blockPos, interactionHand, claimedChunk, entity) -> {
        if (claimedChunk != null) {
            if (claimedChunk.getTeamData().getTeam().getProperty(ALLOW_SHOP_USAGE)) {
                return ProtectionPolicy.ALLOW;
            } else {
                return ProtectionPolicy.DENY;
            }
        }
        return ProtectionPolicy.ALLOW;
    };


    private void teamConfig(TeamCollectPropertiesEvent event) {
        event.add(ALLOW_SHOP_USAGE);
    }

    private EventResult click(Player player, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.level().getBlockState(blockPos).getBlock() instanceof AbstractTradingBlock bb) {
                ClaimedChunkManagerImpl mgr = ClaimedChunkManagerImpl.getInstance();
                if (mgr.shouldPreventInteraction(player, interactionHand, blockPos, USE_SHOP, null)) {
                    FTBCUtils.forceHeldItemSync(serverPlayer, interactionHand);
                    return EventResult.interruptFalse();
                }
            }
        }
        return EventResult.pass();
    }
}
