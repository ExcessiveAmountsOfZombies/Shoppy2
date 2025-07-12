package com.epherical.shoppy.network;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.mojang.logging.LogUtils;
import net.minecraft.world.SimpleMenuProvider;
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
                ), buf -> buf.writeBlockPos(bartering.getBlockPos()));
            } else {
                LOGGER.warn("Player {} tried to open a bartering block as an owner and they aren't the owner... ", player.getScoreboardName());
            }
        }
    }


}
