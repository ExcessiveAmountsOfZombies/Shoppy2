package com.epherical.shoppy.network;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.payloads.AddItemRequestPayload;
import com.epherical.shoppy.network.payloads.PriceSubmissionPayload;
import com.epherical.shoppy.network.payloads.SetSaleItemPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

public class ServerPayloadHandler {


    private static final Logger LOGGER = LogUtils.getLogger();


    public static void handle(AddItemRequestPayload payload, IPayloadContext ctx) {
        var player = ctx.player();
        var be = player.level().getBlockEntity(payload.pos());
        if (be instanceof BarteringBlockEntity bbe && player.containerMenu instanceof BarteringMenuOwner bartering) {

            if (bbe.getOwner().equals(player.getUUID())) {
                player.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> BarteringMenuOwner.barteringOwner(id, bartering.getBlockPos(), true),
                        bbe.getName()
                ), buf -> {
                    buf.writeBlockPos(bartering.getBlockPos());
                    buf.writeBoolean(true);
                });
            } else {
                LOGGER.warn("Player {} tried to open a bartering block as an owner and they aren't the owner... ", player.getScoreboardName());
            }
        }
    }

    public static void handle(SetSaleItemPayload payload, IPayloadContext ctx) {
        var player = ctx.player();

        if (player.containerMenu instanceof BarteringMenuOwner bartering) {
            var be = player.level().getBlockEntity(bartering.getBlockPos());


            if (!(be instanceof BarteringBlockEntity bbe)) {
                return;
            }

            if (!bbe.getOwner().equals(player.getUUID())) {
                LOGGER.warn("Player {} tried to change sale item but is not owner", player.getScoreboardName());
                return;
            }

            ItemStack copy = payload.stack().copyWithCount(1);

            // todo; check if there are already items in the shop first. it'll have to be cleared out before setting.

            if (payload.currency()) {
                bbe.setCurrency(copy);
                player.sendSystemMessage(Component.literal("Set the currency item successfully"));
            } else {
                bbe.setSaleItem(copy);
                player.sendSystemMessage(Component.literal("Set the sale item successfully"));
            }
            bbe.setChanged();

            ServerPlayer player1 = (ServerPlayer) player;
            player.closeContainer();
            player1.connection.send(bbe.getUpdatePacket());
        }
    }
    public static void handle(PriceSubmissionPayload payload, IPayloadContext ctx) {
        var player = ctx.player();

        if (!(player.containerMenu instanceof BarteringMenuOwner menu))
            return;



        var be = player.level().getBlockEntity(menu.getBlockPos());
        if (!(be instanceof BarteringBlockEntity bbe))
            return;

        if (!bbe.getOwner().equals(player.getUUID()))
            return;                      // todo; log

        if (payload.price() == 0)
            return;                       // todo; send to player about bad submission

        if (payload.received() == 0) {
            return;                     // todo; send to player about bad submission
        }

        bbe.setOffer(payload.offerIndex(), payload.price(), payload.received());
        bbe.setChanged();

        ServerPlayer player1 = (ServerPlayer) player;

        menu.setEditing(false);
        player1.connection.send(bbe.getUpdatePacket());


        MenuProvider menuProvider = bbe.getBlockState().getMenuProvider(player1.level(), bbe.getBlockPos());
        if (menuProvider != null) {
            player1.openMenu(menuProvider, buf -> {
                buf.writeBlockPos(bbe.getBlockPos());
                buf.writeBoolean(false);
            });
        }
    }
}
