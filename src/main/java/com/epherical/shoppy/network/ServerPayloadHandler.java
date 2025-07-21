package com.epherical.shoppy.network;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.payloads.AddItemRequestPayload;
import com.epherical.shoppy.network.payloads.PriceSubmissionPayload;
import com.epherical.shoppy.network.payloads.PurchaseAttemptPayload;
import com.epherical.shoppy.network.payloads.SetSaleItemPayload;
import com.epherical.shoppy.network.payloads.StockTransferPayload;
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
                        (id, inv, p) -> BarteringMenuOwner.barteringOwner(id, bartering.getBlockPos(), true, bartering.getContainerData()),
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

            if (bbe.getSaleItemCount() > 0 || bbe.getCurrencyItemCount() > 0) {
                player.displayClientMessage(Component.translatable(
                        "message.shoppy.cannot_change_items_with_stock"), false);
                return;
            }

            if (payload.currency()) {
                bbe.setCurrency(copy);
                player.displayClientMessage(Component.literal("Set the currency item successfully"), false);
            } else {
                bbe.setSaleItem(copy);
                player.displayClientMessage(Component.literal("Set the sale item successfully"), false);
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

    public static void handle(PurchaseAttemptPayload payload, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        int idx = payload.offerIndex();

        if (!(player.containerMenu instanceof BarteringMenu bartering)) return;
        BarteringBlockEntity be = (BarteringBlockEntity) player.level().getBlockEntity(bartering.getBlockPos());

        if (be == null) return;
        // todo; dont let owner purhcase

        be.tryPurchase(player, idx);
    }


    public static void handle(StockTransferPayload payload, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        if (!(player.level().getBlockEntity(payload.pos()) instanceof BarteringBlockEntity shop))
            return;   // invalid / out-of-range

        if (!shop.getOwner().equals(player.getUUID())) {
            // don't let none owners do anything...
            return;
        }

        boolean saleSide = payload.saleSide();
        boolean inserting = payload.insert();

        if (!saleSide && inserting) {
            return;  // we don't want the owner inserting into
        }


        // resolve the template item (“what are we moving?”)
        ItemStack template = saleSide ? shop.getSaleItem() : shop.getCurrencyItem();
        if (template.isEmpty()) return;

        int moved = 0;
        if (inserting) {  /* player → shop ---------------------------------- */
            int free = shop.getFreeSlots();        // max space left in the counter
            if (free <= 0) return;

            for (ItemStack inv : player.getInventory().getNonEquipmentItems()) {
                if (ItemStack.isSameItem(inv, template) && inv.getCount() > 0) {
                    int take = Math.min(inv.getCount(), free - moved);
                    inv.shrink(take);
                    moved += take;
                    if (moved >= free) break;
                }
            }
            if (moved == 0) return;                // nothing found

            if (saleSide) shop.addSaleItems(moved);
            else          shop.addCurrencyItems(moved);
        } else {        /* shop → player ------------------------------------ */
            int available = saleSide ? shop.getSaleItemCount()
                    : shop.getCurrencyItemCount();
            if (available <= 0) return;

            moved = Math.min(available, template.getMaxStackSize());
            ItemStack give = template.copy();
            give.setCount(moved);

            if (!player.getInventory().add(give))      // full inventory? -> drop
                player.drop(give, false);

            if (saleSide) shop.addSaleItems(-moved);
            else          shop.addCurrencyItems(-moved);
        }

        shop.setChanged();
        player.containerMenu.broadcastChanges();   // sync GUI counters
    }
}
